package com.adamfalcone.dbstreams;

import com.adamfalcone.avro.AssociateSalesCount;
import com.adamfalcone.kafka.constants.ConfigConstants;
import com.adamfalcone.utils.WindowedSerde;
import io.confluent.connect.avro.ConnectDefault;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.connect.data.Decimal;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.HostInfo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class SalesFiveMinuteAvgQueryable {

    static final String SALES_TRANSACTION_STREAM = "pg-sales-transaction";
    static final String SALES_FIVE_MIN_AVG_BY_ASSOC_STORE = "sales-five-minute-avg-by-assoc";

    public static void main(final String[] args) throws Exception {
        final String bootstrapServers = args.length > 0 ? args[0] : ConfigConstants.BOOTSTRAP_SERVERS;
        final String schemaRegistry = args.length > 1 ? args[1] : ConfigConstants.SCHEMA_REGISTRY;
        final String stateDir = args.length > 2 ? args[2] : ConfigConstants.STATE_DIR;
        final String restEndpointHostName = args.length > 3 ? args[3] : ConfigConstants.REST_ENDPOINT_HOSTNAME;
        final Integer restEndpointPort = args.length > 4 ? Integer.valueOf(args[4]) : ConfigConstants.REST_ENDPOINT_PORT;

        final HostInfo hostInfo = new HostInfo(restEndpointHostName, restEndpointPort);

        final KafkaStreams streams = buildFiveMinuteSalesStream(bootstrapServers,
                schemaRegistry,
                stateDir,
                hostInfo);

        streams.cleanUp();

        streams.start();

        // Start the Restful proxy for servicing remote access to state stores
        final SalesRestSrv restService = startRestProxy(streams, hostInfo);


        //add shutdown hook to respond gracefully to SIGTERM and close app
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Gracefully shutting down " + streams.getClass().getName());
                streams.close();
            }
        }));
    }

    static KafkaStreams buildFiveMinuteSalesStream(final String bootstrapServers,
                                                   final String schemaRegistryUrl,
                                                   final String stateDir,
                                                   final HostInfo hostInfo) {
        final Properties streamsConfig = new Properties();
        streamsConfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamsConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, "sales-five-min-assoc-avg");
        streamsConfig.put(StreamsConfig.CLIENT_ID_CONFIG, "sales-five-min-assoc-avg-client");
        streamsConfig.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        streamsConfig.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfig.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        streamsConfig.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);
        streamsConfig.put(StreamsConfig.APPLICATION_SERVER_CONFIG, hostInfo.host() + ":" + hostInfo.port());
        streamsConfig.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        final Schema decimalSchema = new Decimal().builder(2);
        final Serde<Windowed<String>> windowedStringSerde = new WindowedSerde<>(Serdes.String());
        final Serde<String> stringSerde = new Serdes.StringSerde();
        final Serde<Integer> integerSerde = new Serdes.IntegerSerde();

        //avro schemas for registry
        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);

        final Serde<AssociateSalesCount> valueAvroSerde = new SpecificAvroSerde<>();
        valueAvroSerde.configure(serdeConfig, false);

        final KStreamBuilder builder = new KStreamBuilder();

        //hook into topic
        final KStream<String, ConnectDefault> transactions = builder.stream(SALES_TRANSACTION_STREAM);

        //rekey the stream
        final KStream<String, ConnectDefault> rekeyedStream = transactions
                .map((dummy,val) -> new KeyValue<>(val.getSalesPerson().toString(), val));

        //group by key to create changelog
        final KGroupedStream<String, ConnectDefault> groupedSalesByAssoc = rekeyedStream
                .groupByKey();

        //

        final KTable<Windowed<String>,AssociateSalesCount> countAndAvg = groupedSalesByAssoc
                .aggregate(
                        () -> new AssociateSalesCount("","0.00",0,"0.00"),
                        (aggKey, newVal, aggVal) -> {
                            aggVal.setSalesPerson(newVal.getSalesPerson());
                            aggVal.setTransactionCount(aggVal.getTransactionCount() + 1);
                            BigDecimal currentTotalSales = new BigDecimal(aggVal.getTotalSales().toString());
                            BigDecimal newSale = Decimal.toLogical(decimalSchema, newVal.getTotalSale().array());
                            BigDecimal newTotalSales = currentTotalSales.add(newSale);
                            BigDecimal newAvgSales = newSale.add(currentTotalSales).divide(new BigDecimal(aggVal.getTransactionCount()),2,RoundingMode.HALF_UP);
                            aggVal.setTotalSales(newTotalSales.toString());
                            aggVal.setAverageSale(newAvgSales.toString());
                            return aggVal;
                        },
                        TimeWindows.of(TimeUnit.MINUTES.toMillis(5)),
                        valueAvroSerde,
                        SALES_FIVE_MIN_AVG_BY_ASSOC_STORE
                );

        final KStream<Windowed<String>,AssociateSalesCount> streamForPrint = countAndAvg.toStream();

        streamForPrint.foreach(
                (k,v) -> System.out.println(k.key() + " *** " + v)
        );

        return new KafkaStreams(builder,streamsConfig);
    }

    static SalesRestSrv startRestProxy(final KafkaStreams streams, final HostInfo hostInfo)
            throws Exception {
        final SalesRestSrv interactiveQueriesRestSrv = new SalesRestSrv(streams, hostInfo);
        interactiveQueriesRestSrv.start();
        return interactiveQueriesRestSrv;
    }
}
