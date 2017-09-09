package com.adamfalcone.dbstreams;

import com.adamfalcone.kafka.constants.ConfigConstants;
import com.adamfalcone.utils.WindowedSerde;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.connect.data.Decimal;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Timestamp;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import io.confluent.connect.avro.ConnectDefault;
import org.apache.kafka.streams.kstream.internals.TimeWindow;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class SalesOneMinAgg {
    public static void main(final String[] args) throws Exception {
        final String bootstrapServers = args.length > 0 ? args[0] : ConfigConstants.BOOTSTRAP_SERVERS;
        final String schemaRegistry = args.length > 1 ? args[1] : ConfigConstants.SCHEMA_REGISTRY;
        final String stateDir = args.length > 2 ? args[2] : ConfigConstants.STATE_DIR;
        final KafkaStreams streams = buildOneMinSalesAggStream(bootstrapServers,
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

    static KafkaStreams buildOneMinSalesAggStream(final String bootstrapServers,
                                                  final String schemaRegistryUrl,
                                                  final String stateDir) throws IOException {
        final Properties streamConfiguration = new Properties();
        streamConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "one-minute-sales-by-assoc");
        streamConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "one-minute-sales-by-assoc-client");
        streamConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        streamConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        streamConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);
        streamConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        final Schema decimalSchema = new Decimal().builder(2);
        //final Schema timestampSchema = new Timestamp().builder();

        final Serde<Windowed<String>> windowedStringSerde = new WindowedSerde<>(Serdes.String());
        final Serde<String> strSerde = new Serdes.StringSerde();

        final KStreamBuilder builder = new KStreamBuilder();

        final KStream<String, ConnectDefault> transactions = builder.stream("pg-sales-transaction");

        final KStream<String, ConnectDefault> rekeyed = transactions
                .map((dummy, val) -> new KeyValue<>(val.getSalesPerson().toString(),val));

        final KGroupedStream<String, String> groupedSalesByAssoc = rekeyed
                .map(
                        (k,v) -> new KeyValue<>(k, Decimal.toLogical(decimalSchema, v.getTotalSale().array()).toString())
                )
                .groupByKey(strSerde, strSerde);

        final KTable<Windowed<String>,String> salesOneMinByAssoc = groupedSalesByAssoc
                .aggregate(
                        () -> "0.00",
                        (aggKey, newVal, aggVal) -> {
                            System.out.println(aggVal);
                            BigDecimal av = new BigDecimal(aggVal);
                            BigDecimal bd = new BigDecimal(newVal);
                            bd = bd.add(av);
                            return bd.toString();
                        },
                        TimeWindows.of(TimeUnit.MINUTES.toMillis(1)),
                        strSerde,
                        "sales-one-minute-by-assoc-v3"
                );

        final KStream<Windowed<String>,String> streamForPrint = salesOneMinByAssoc.toStream();

        streamForPrint.foreach(
                (k,v) -> System.out.println(k + " *** " + v)
        );

        return new KafkaStreams(builder,streamConfiguration);
    }
}
