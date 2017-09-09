package com.adamfalcone.dbstreams;

import com.adamfalcone.kafka.constants.ConfigConstants;
import com.adamfalcone.utils.WindowedSerde;
import io.confluent.connect.avro.ConnectDefault;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
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
import org.apache.kafka.streams.state.ReadOnlyWindowStore;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class SalesOneMinAvgQueryable {

    static final String SALES_TRANSACTION_STREAM = "pg-sales-transaction";
    static final String SALES_ONE_MIN_CT_BY_ASSOC_STORE = "sales-one-minute-ct-by-assoc";
    static final String SALES_ONE_MIN_AVG_BY_ASSOC_STORE = "sales-one-minute-avg-by-assoc";
    static final String SALES_ONE_MIN_ASSOC_AVG = "sales-one-minute-assoc-avg";

    public static void main(final String[] args) throws Exception {
        final String bootstrapServers = args.length > 0 ? args[0] : ConfigConstants.BOOTSTRAP_SERVERS;
        final String schemaRegistry = args.length > 1 ? args[1] : ConfigConstants.SCHEMA_REGISTRY;
        final String stateDir = args.length > 2 ? args[2] : ConfigConstants.STATE_DIR;
        final String restEndpointHostName = args.length > 3 ? args[3] : ConfigConstants.REST_ENDPOINT_HOSTNAME;
        final Integer restEndpointPort = args.length > 4 ? Integer.valueOf(args[4]) : ConfigConstants.REST_ENDPOINT_PORT;

        final HostInfo restEndpoint = new HostInfo(restEndpointHostName, restEndpointPort);

        final KafkaStreams streams = buildOneMinSalesAggStream(bootstrapServers,
                schemaRegistry,
                stateDir,
                restEndpoint);

        streams.cleanUp();

        streams.start();

        // Start the Restful proxy for servicing remote access to state stores
        final SalesOneMinAvgRestSrv restService = startRestProxy(streams, restEndpoint);


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
                                                  final String stateDir,
                                                  final HostInfo hostInfo) throws IOException {
        final Properties streamConfiguration = new Properties();
        streamConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        streamConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "one-minute-sales-avg-by-assoc");
        streamConfiguration.put(StreamsConfig.CLIENT_ID_CONFIG, "one-minute-sales-avg-by-assoc-client");
        streamConfiguration.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
        streamConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, SpecificAvroSerde.class);
        streamConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, stateDir);
        streamConfiguration.put(StreamsConfig.APPLICATION_SERVER_CONFIG, hostInfo.host() + ":" + hostInfo.port());
        streamConfiguration.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        final Schema decimalSchema = new Decimal().builder(2);
        //final Schema timestampSchema = new Timestamp().builder();

        final Serde<Windowed<String>> windowedStringSerde = new WindowedSerde<>(Serdes.String());
        final Serde<String> strSerde = new Serdes.StringSerde();
        final Serde<Integer> intSerde = new Serdes.IntegerSerde();

        final KStreamBuilder builder = new KStreamBuilder();

        //hook into topic
        final KStream<String, ConnectDefault> transactions = builder.stream(SALES_TRANSACTION_STREAM);

        //re-key the stream
        final KStream<String, ConnectDefault> rekeyed = transactions
                .map((dummy, val) -> new KeyValue<>(val.getSalesPerson().toString(),val));

        //group by key and convert byte array to decimal
        final KGroupedStream<String, String> groupedSalesByAssoc = rekeyed
                .map(
                        (k,v) -> new KeyValue<>(k, Decimal.toLogical(decimalSchema, v.getTotalSale().array()).toString())
                )
                .groupByKey(strSerde, strSerde);

        //create a state store of sale count by associate for the last minute
        final KTable<Windowed<String>, Integer> salesCtOneMinByAssoc = groupedSalesByAssoc
                .aggregate(
                        () -> 0,
                        (aggKey, newVal, aggVal) -> aggVal + 1,
                        TimeWindows.of(TimeUnit.MINUTES.toMillis(1)),
                        intSerde,
                        SALES_ONE_MIN_CT_BY_ASSOC_STORE
                );

        //create a state store of aggregated sales by associate for the last minute
        final KTable<Windowed<String>,String> salesOneMinByAssoc = groupedSalesByAssoc
                .aggregate(
                        () -> "0.00",
                        (aggKey, newVal, aggVal) -> {
                            //System.out.println(aggVal);
                            BigDecimal av = new BigDecimal(aggVal);
                            BigDecimal bd = new BigDecimal(newVal);
                            bd = bd.add(av);
                            return bd.toString();
                        },
                        TimeWindows.of(TimeUnit.MINUTES.toMillis(1)),
                        strSerde,
                        SALES_ONE_MIN_AVG_BY_ASSOC_STORE
                );

        //join the two previous state stores and calculate the average sales by associate (for the last minute)
        final KTable<Windowed<String>, String> salesOneMinAvgByAssoc = salesCtOneMinByAssoc.join(
                salesOneMinByAssoc,
                (leftVal, rightVal) -> {
                    System.out.println(leftVal.toString() + " ||| " + rightVal.toString());
                    BigDecimal totalSales = new BigDecimal(rightVal);
                    BigDecimal avgSales = totalSales.divide(new BigDecimal(leftVal.toString()), 2, RoundingMode.HALF_UP);
                    return avgSales.toString();
                });

        //convert the KTable back to a KStream
        final KStream<Windowed<String>,String> streamForPrint = salesOneMinAvgByAssoc.toStream();

        //print to the console to simply see the results
        streamForPrint.foreach(
                (k,v) -> System.out.println(k.key() + " *** " + v)
        );

        return new KafkaStreams(builder,streamConfiguration);
    }

    static SalesOneMinAvgRestSrv startRestProxy(final KafkaStreams streams, final HostInfo hostInfo)
        throws Exception {
            final SalesOneMinAvgRestSrv interactiveQueriesRestSrv = new SalesOneMinAvgRestSrv(streams, hostInfo);
            interactiveQueriesRestSrv.start();
            return interactiveQueriesRestSrv;
    }
}
