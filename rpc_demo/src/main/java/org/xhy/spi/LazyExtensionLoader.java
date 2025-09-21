package org.xhy.spi;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 *  真正的按需加载 SPI 实现
 * TODO 与注册中心结合：注册中心监听SPI节点变化，
 *      LazyExtensionLoader.reload(interfaceClass) 清除缓存，
 *      等待下一次 get(interfaceClass, key) 时加载最新插件实现
 */
public class LazyExtensionLoader {

    // 系统SPI
    private static final String SYS_EXTENSION_LOADER_DIR_PREFIX = "META-INF/xrpc/";

    // 用户SPI
    private static final String DIY_EXTENSION_LOADER_DIR_PREFIX = "META-INF/rpc/";

    private static final String[] PREFIXS = {SYS_EXTENSION_LOADER_DIR_PREFIX, DIY_EXTENSION_LOADER_DIR_PREFIX};

    // 三级缓存结构
    private static final Map<String, Map<String, String>> CONFIG_CACHE = new ConcurrentHashMap<>(); // 某个接口类的所有配置：接口名 -> (key -> 类名)
    private static final Map<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();           // key -> 类对象
    private static final Map<String, Object> INSTANCE_CACHE = new ConcurrentHashMap<>();          // key -> 实例

    // 按接口分类的锁
    private static final Map<String, ReentrantLock> INTERFACE_LOCKS = new ConcurrentHashMap<>();

    private static final LazyExtensionLoader extensionLoader = new LazyExtensionLoader();

    public static LazyExtensionLoader getInstance() {
        return extensionLoader;
    }

    private LazyExtensionLoader() {
    }

    /**
     * 获取扩展点实例（按需加载）
     *
     * @param key 配置文件中定义的key
     * @return 扩展点实例
     */
    public <T> T get(Class<T> interfaceClass, String key) {
        // 确保对应接口的配置已加载
        ensureConfigLoaded(interfaceClass);

        // 以下逻辑保持不变
        if (INSTANCE_CACHE.containsKey(key)) {
            return (T) INSTANCE_CACHE.get(key);
        }

        ReentrantLock lock = getInterfaceLock(key);
        lock.lock();
        try {
            if (INSTANCE_CACHE.containsKey(key)) {
                return (T) INSTANCE_CACHE.get(key);
            }

            Class<?> clazz = loadClass(key);
            if (clazz == null) {
                throw new IllegalStateException("No extension found for key: " + key);
            }

            Object instance = clazz.newInstance();
            INSTANCE_CACHE.put(key, instance);
            return (T) instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create extension instance for key: " + key, e);
        } finally {
            lock.unlock();
        }
    }

//    public <T> T get(String key) {
//        // 1. 检查实例缓存
//        if (INSTANCE_CACHE.containsKey(key)) {
//            return (T) INSTANCE_CACHE.get(key);
//        }
//
//        // 2. 双重检查锁确保线程安全
//        ReentrantLock lock = getInterfaceLock(key);
//        lock.lock();
//        try {
//            if (INSTANCE_CACHE.containsKey(key)) {
//                return (T) INSTANCE_CACHE.get(key);
//            }
//
//            // 3. 按需加载类
//            Class<?> clazz = loadClass(key);
//            if (clazz == null) {
//                throw new IllegalStateException("No extension found for key: " + key);
//            }
//
//            // 4. 创建实例并缓存
//            Object instance = clazz.newInstance();
//            INSTANCE_CACHE.put(key, instance);
//            return (T) instance;
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to create extension instance for key: " + key, e);
//        } finally {
//            lock.unlock();
//        }
//    }

    /**
     * 获取接口的所有实现实例
     *
     * @param interfaceClass 接口类
     * @return 实现实例列表
     */
    public <T> List<T> gets(Class<T> interfaceClass) {
        String interfaceName = interfaceClass.getName();

        // 1. 确保配置已加载
        ensureConfigLoaded(interfaceClass);

        // 2. 获取该接口的所有key
        Map<String, String> configMap = CONFIG_CACHE.get(interfaceName);
        if (configMap == null || configMap.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 按需加载所有实现
        List<T> instances = new ArrayList<>();
        for (String key : configMap.keySet()) {
            instances.add(get(interfaceClass, key));
        }
        return instances;
    }

    /**
     * 按需加载类
     */
    private Class<?> loadClass(String key) {
        // 1. 检查类缓存
        if (CLASS_CACHE.containsKey(key)) {
            return CLASS_CACHE.get(key);
        }

        // 2. 查找类名
        String className = findClassName(key);
        if (className == null) {
            throw new IllegalStateException("No configuration found for key: " + key);
        }

        // 3. 加载类
        try {
            Class<?> clazz = Class.forName(className);
            CLASS_CACHE.put(key, clazz);
            return clazz;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Extension class not found: " + className, e);
        }
    }

    /**
     * 查找key对应的类名
     */
    private String findClassName(String key) {
        for (Map<String, String> configMap : CONFIG_CACHE.values()) {
            if (configMap.containsKey(key)) {
                return configMap.get(key);
            }
        }
        return null;
    }

    /**
     * 确保配置已加载
     */
    private void ensureConfigLoaded(Class<?> interfaceClass) {
        String interfaceName = interfaceClass.getName();

        // 双重检查锁确保线程安全
        if (!CONFIG_CACHE.containsKey(interfaceName)) {
            ReentrantLock lock = getInterfaceLock(interfaceName);
            lock.lock();
            try {
                if (!CONFIG_CACHE.containsKey(interfaceName)) {
                    loadConfig(interfaceClass);
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 加载配置（不加载类）
     */
    private void loadConfig(Class<?> interfaceClass) throws IllegalStateException {
        String interfaceName = interfaceClass.getName();
        Map<String, String> configMap = new HashMap<>();

        try {
            ClassLoader classLoader = this.getClass().getClassLoader();

            for (String prefix : PREFIXS) {
                String spiFilePath = prefix + interfaceName;
                Enumeration<URL> urls = classLoader.getResources(spiFilePath);

                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(url.openStream()))) {

                        String line;
                        while ((line = reader.readLine()) != null) {
                            String[] parts = line.split("=");
                            if (parts.length != 2) continue;

                            String key = parts[0].trim();
                            String className = parts[1].trim();

                            // 只缓存配置，不加载类
                            configMap.put(key, className);
                        }
                    }
                }
            }

            CONFIG_CACHE.put(interfaceName, configMap);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SPI configuration for interface: " + interfaceName, e);
        }
    }

    /**
     * 获取接口对应的锁
     */
    private ReentrantLock getInterfaceLock(String identifier) {
        return INTERFACE_LOCKS.computeIfAbsent(identifier, k -> new ReentrantLock());
    }
}