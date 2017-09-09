package com.adamfalcone.dbstreams;

import com.adamfalcone.avro.AssociateSalesTransactions;
import com.adamfalcone.avro.SalesTransactionSummary;
import com.adamfalcone.kafka.constants.ConfigConstants;
import com.adamfalcone.pojo.AssocSalesTransactions;
import com.adamfalcone.pojo.TransactionSummary;
import com.adamfalcone.utils.WindowedSerde;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.connect.data.Decimal;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Timestamp;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import io.confluent.connect.avro.ConnectDefault;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;

public class ComplexTypeJoin {
    public static void  main (String[] args) throws Exception {
        final String bootstrapServers = args.length > 0 ? args[0] : ConfigConstants.BOOTSTRAP_SERVERS;
        final String schemaRegistry = args.length > 1 ? args[1] : ConfigConstants.SCHEMA_REGISTRY;
        final String stateDir = args.length > 2 ? args[2] : ConfigConstants.STATE_DIR;
        final KafkaStreams streams = buildComplexTypeJoinStream(bootstrapServers,
                schemaRegistry,
                stateDir);
        streams.cleanUp();
        streams.start();

        //add shutdown hook to respond gracefully to SIGTERM and close app
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Gracefully shutting down " + streams.getClass().getName());
                streams.close();
            }
        }));
    }

    static KafkaStreams buildComplexTypeJoinStream(final String bootstrapServers,
                                                  final String schemaRegistryUrl,
                                                  final String stateDir) throws IOException {
        // configure the stream
        final Properties streamConfiguration = new Properties();
        streamConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "complex-type-join");
        streamConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "complex-type-join-client");
        streamConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        streamConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        streamConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);
        streamConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        // interim schemas
        final Schema decimalSchema = new Decimal().builder(2);
        final Schema timestampSchema = new Timestamp().builder();

        // declare additional serde overrides
        final Serde<String> strSerde = new Serdes.StringSerde();
        final Serde<Integer> intSerde = new Serdes.IntegerSerde();

        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

        final Serde<GenericRecord> stringAvroSerde = new GenericAvroSerde();
        stringAvroSerde.configure(serdeConfig, true);

        final SpecificAvroSerde<AssociateSalesTransactions> assocSalesTransSerde = new SpecificAvroSerde<>();
        assocSalesTransSerde.configure(serdeConfig, false);

        // begin the topology
        final KStreamBuilder builder = new KStreamBuilder();

        // open the transactions stream
        final KStream<String, ConnectDefault> transactions = builder.stream("pg-sales-transaction");

        // rekey the events (key is currently null)
        final KStream<String, ConnectDefault> rekeyed = transactions
                .map(
                        (dummy, v) -> new KeyValue<>(v.getSalesPerson().toString(), v)
                );

        // create a grouped stream so we can begin aggregations
        final KGroupedStream<String, ConnectDefault> rekeyedGroup = rekeyed
                .groupByKey();

        // convert the incoming event values
        final KGroupedStream<String, AssociateSalesTransactions> groupedSalesByAssociate = rekeyed
                .map(
                        (k,v) ->  {
                            SalesTransactionSummary sale = SalesTransactionSummary.newBuilder()
                                    .setId(v.getId())
                                    .setSalesPerson(v.getSalesPerson())
                                    .setFirstName(v.getFirstName())
                                    .setLastName(v.getLastName())
                                    .setTotalSale(v.getTotalSale())
                                    .setTransactionDate(v.getTransactionDate())
                                    .build();
                            List<SalesTransactionSummary> transList = Arrays.asList(sale);
                            AssociateSalesTransactions val = AssociateSalesTransactions.newBuilder()
                                    .setSalesPerson(v.getSalesPerson())
                                    .setTransactionCount(1)
                                    .setTransactions(transList)
                                    .build();
                            return new KeyValue<>(k, val);
                        }
                )
                .groupByKey();

        // create the materialized view of assocSalesTrans
        final KTable<Windowed<String>, AssociateSalesTransactions> assocSalesTransMV = groupedSalesByAssociate
                .aggregate(
                        () ->  {
                            List<SalesTransactionSummary> list = new ArrayList<>();
                            return AssociateSalesTransactions.newBuilder()
                                    .setSalesPerson("")
                                    .setTransactionCount(0)
                                    .setTransactions(list)
                                    .build();
                        },
                        (aggKey, newVal, aggVal) -> {
                            List<SalesTransactionSummary> aggList = aggVal.getTransactions();
                            aggList.addAll(newVal.getTransactions());
                            aggVal.setTransactions(aggList);
                            aggVal.setTransactionCount(aggVal.getTransactionCount() + newVal.getTransactionCount());
                            aggVal.setSalesPerson(newVal.getSalesPerson());
                            return aggVal;
                        },
                        TimeWindows.of(TimeUnit.MINUTES.toMillis(2)),
                        assocSalesTransSerde,
                        "assoc-sales-trans-mv"
                );

        final KStream<Windowed<String>,AssociateSalesTransactions> streamForPrint = assocSalesTransMV.toStream();

        streamForPrint.foreach(
                (k,v) -> System.out.println(k.key() + " *** " + v)
        );

        return new KafkaStreams(builder,streamConfiguration);
    }
}
