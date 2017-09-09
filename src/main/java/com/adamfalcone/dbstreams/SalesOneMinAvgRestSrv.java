package com.adamfalcone.dbstreams;

import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.*;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  A RestAPI to expose the Sales One Min Avg materialized view data
 */

@Path("kafka-one-min-avg-sales")
public class SalesOneMinAvgRestSrv {

    private final KafkaStreams streams;
    private final MetadataService metadataService;
    private final HostInfo hostInfo;
    private final Client client = ClientBuilder.newBuilder().register(JacksonFeature.class).build();
    private Server jettyServer;

    public SalesOneMinAvgRestSrv(KafkaStreams streams, HostInfo hostInfo) {
        this.streams = streams;
        this.hostInfo = hostInfo;
        this.metadataService = new MetadataService(streams);
    }

    @GET()
    @Path("/associate/{associate}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String,String>> oneMinAvgSalesForAssociate (@PathParam("associate") final String assoc) {

        // The genre might be hosted on another instance. We need to find which instance it is on
        // and then perform a remote lookup if necessary.
        final HostStoreInfo host = metadataService.
                streamsMetadataForStoreAndKey(SalesOneMinAvgQueryable.SALES_ONE_MIN_AVG_BY_ASSOC_STORE, assoc, new StringSerializer());

        // genre is on another instance. call the other instance to fetch the data.
//        if (!thisHost(host)) {
//            return fetchAssociateAvg(host, "kafka-one-min-avg-sales/associate/" + assoc);
//        }

        // genre is on this instance
        return getOneMinAvgSales(assoc.toLowerCase(), SalesOneMinAvgQueryable.SALES_ONE_MIN_AVG_BY_ASSOC_STORE,0, 9223372033453345334L);
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

    private boolean thisHost(final HostStoreInfo host) {
        return host.getHost().equals(hostInfo.host()) &&
                host.getPort() == hostInfo.port();
    }

    private String fetchAssociateAvg(final HostStoreInfo host, final String path) {
        return client.target(String.format("http://%s%d/%s", host.getHost(), host.getPort(), path))
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<String>(){});
    }

    private List<Map<String,String>> getOneMinAvgSales (final String key, final String storeName, final long from, final long to) {

        final ReadOnlyWindowStore<String, String> avgSalesStore = streams.store(storeName, QueryableStoreTypes.<String,String>windowStore());

        // fetch the window results for the given key and time range
        final WindowStoreIterator<String> results = avgSalesStore.fetch(key, from, to);

        if (results == null) {
            throw new NotFoundException(String.format("Unable to find value in %s for key %s", storeName, key));
        }

        final List<Map<String,String>> windowResults = new ArrayList<>();

        while (results.hasNext()) {
            final KeyValue<Long, String> next = results.next();
            // convert the result to have the window time and the key (for display purposes)

            windowResults.add(new HashMap<String, String>(){{
                put(next.key.toString(), next.value);
            }});
        }
        return windowResults;
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
