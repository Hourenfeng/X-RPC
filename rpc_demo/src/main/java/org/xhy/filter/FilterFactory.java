package org.xhy.filter;

import org.xhy.filter.client.ClientAfterFilter;
import org.xhy.filter.client.ClientBeforeFilter;
import org.xhy.filter.server.ServerAfterFilter;
import org.xhy.filter.server.ServerBeforeFilter;
import org.xhy.spi.ExtensionLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FilterFactory {


    public static List<Filter> getClientBeforeFilters() {
        return ExtensionLoader.getInstance().gets(ClientBeforeFilter.class);

    }

    public static List<Filter> getClientAfterFilters() {
        return ExtensionLoader.getInstance().gets(ClientAfterFilter.class);

    }

    public static List<Filter> getServerBeforeFilters() {
        return ExtensionLoader.getInstance().gets(ServerBeforeFilter.class);

    }

    public static List<Filter> getServerAfterFilters() {
        return ExtensionLoader.getInstance().gets(ServerAfterFilter.class);
    }


    public static void initClient() throws IOException, ClassNotFoundException {
        final ExtensionLoader extensionLoader = ExtensionLoader.getInstance();
        extensionLoader.loadExtension(ClientAfterFilter.class);
        extensionLoader.loadExtension(ClientBeforeFilter.class);
    }

    public static void initServer() throws IOException, ClassNotFoundException {
        final ExtensionLoader extensionLoader = ExtensionLoader.getInstance();
        extensionLoader.loadExtension(ServerAfterFilter.class);
        extensionLoader.loadExtension(ServerBeforeFilter.class);
    }
}
