package com.adamfalcone.dbstreams;

import com.adamfalcone.avro.AssociateSalesCount;
import com.adamfalcone.pojo.AssociateSalesData;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import io.confluent.examples.streams.interactivequeries.*;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 *  RestAPI to expose the Sales materialized view data
 */
@Path("kafka-sales-mv")
public class SalesRestSrv {
    private final KafkaStreams streams;
    private final MetadataService metadataService;
    private final HostInfo hostInfo;
    private final Client client = ClientBuilder.newBuilder().register(JacksonFeature.class).build();
    private Server jettyServer;

    public SalesRestSrv(KafkaStreams streams, HostInfo hostInfo) {
        this.streams = streams;
        this.hostInfo = hostInfo;
        this.metadataService = new MetadataService(streams);
    }

    @GET
    @Path("/associate/{associate}/{window}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAssociateAvgSales (@PathParam("associate") String associate, @PathParam("window") String window)
            throws JsonProcessingException {

        /* TODO: parameterize this! */
        final HostStoreInfo host = metadataService.
                streamsMetadataForStoreAndKey(SalesFiveMinuteAvgQueryable.SALES_FIVE_MIN_AVG_BY_ASSOC_STORE, associate, new StringSerializer());

        List<Map<String,AssociateSalesData>> assocSalesList = getAssocAvgSales(associate.toLowerCase(), SalesFiveMinuteAvgQueryable.SALES_FIVE_MIN_AVG_BY_ASSOC_STORE,0L, 9223372033453345334L);

        ObjectMapper objectMapper = new ObjectMapper();

        String jsonResults = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(assocSalesList);

        return jsonResults;
    }

    private List<Map<String,AssociateSalesData>> getAssocAvgSales (final String associate, final String store, final long from, final long to) {
        final ReadOnlyWindowStore<String, AssociateSalesCount> salesStore = streams.store(store, QueryableStoreTypes.<String,AssociateSalesCount>windowStore());


        // fetch the window results for the given key and time range
        final WindowStoreIterator<AssociateSalesCount> results = salesStore.fetch(associate, from, to);

        if (results == null) {
            throw new NotFoundException(String.format("Unable to find value in %s for key %s", store, associate));
        }

        final List<Map<String,AssociateSalesData>> windowResults = new ArrayList<>();

        while (results.hasNext()) {
            final KeyValue<Long, AssociateSalesCount> next = results.next();
            // convert the result to have the window time and the key (for display purposes)
            Date date = new Date(next.key);

            windowResults.add(new HashMap<String, AssociateSalesData>(){{
                put(date.toString(), new AssociateSalesData(
                        next.value.getSalesPerson().toString(),
                        next.value.getTotalSales().toString(),
                        next.value.getTransactionCount().toString(),
                        next.value.getAverageSale().toString()
                ));
            }});
        }
        return windowResults;
    }

    /**
     * Get the metadata for all of the instances of this Kafka Streams application
     * @return List of {@link HostStoreInfo}
     */
    @GET()
    @Path("/instances")
    @Produces(MediaType.APPLICATION_JSON)
    public List<HostStoreInfo> streamsMetadata() {
        return metadataService.streamsMetadata();
    }

    /**
     * Get the metadata for all instances of this Kafka Streams application that currently
     * has the provided store.
     * @param store   The store to locate
     * @return  List of {@link HostStoreInfo}
     */
    @GET()
    @Path("/instances/{storeName}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<HostStoreInfo> streamsMetadataForStore(@PathParam("storeName") String store) {
        return metadataService.streamsMetadataForStore(store);
    }

    /**
     * Start an embedded Jetty Server
     * @throws Exception
     */
    void start() throws Exception {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        jettyServer = new Server(hostInfo.port());
        jettyServer.setHandler(context);

        ResourceConfig rc = new ResourceConfig();
        rc.register(this);
        rc.register(JacksonFeature.class);

        ServletContainer sc = new ServletContainer(rc);
        ServletHolder holder = new ServletHolder(sc);
        context.addServlet(holder, "/*");

        jettyServer.start();
    }

    /**
     * Stop the Jetty Server
     * @throws Exception
     */
    void stop() throws Exception {
        if (jettyServer != null) {
            jettyServer.stop();
        }
    }
}
