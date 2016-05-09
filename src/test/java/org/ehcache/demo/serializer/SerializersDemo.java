package org.ehcache.demo.serializer;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.demo.model.Description;
import org.ehcache.demo.model.Employee;
import org.ehcache.impl.config.persistence.CacheManagerPersistenceConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import java.io.File;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by alsu on 18/09/15.
 */
public class SerializersDemo {
  
  private static final String PERSISTENCE_PATH = "/Users/alsu/terracotta";
  
  @Test
  public void testTransientSerializer() throws Exception {
    // tag::transientSerializerGoodSample[]
    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    CacheConfiguration<Long, String> cacheConfig = 
        CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Long.class, String.class, ResourcePoolsBuilder.heap(10).offheap(5, MemoryUnit.MB))  // <1>
        .withValueSerializer(SimpleTransientStringSerializer.class)   // <2>
        .build();

    Cache<Long, String> fruitsCache = cacheManager.createCache("fruitsCache", cacheConfig);
    fruitsCache.put(1L, "apple");
    fruitsCache.put(2L, "orange");
    fruitsCache.put(3L, "mango");
    assertThat(fruitsCache.get(1L), is("apple"));   // <3>
    assertThat(fruitsCache.get(3L), is("mango"));
    assertThat(fruitsCache.get(2L), is("orange"));
    assertThat(fruitsCache.get(1L), is("apple"));
    // end::transientSerializerGoodSample[]
  }

  @Ignore
  @Test
  public void testTransientSerializerWithPersistentCache() throws Exception {
    // tag::transientSerializerBadSample[]
    CacheConfiguration<Long, String> cacheConfig =
        CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Long.class, String.class,
            ResourcePoolsBuilder.heap(10).disk(10, MemoryUnit.MB, true))  // <1>
            .withValueSerializer(SimpleTransientStringSerializer.class)
            .build();

    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .with(new CacheManagerPersistenceConfiguration(new File(PERSISTENCE_PATH)))   // <2>
        .withCache("fruitsCache", cacheConfig)
        .build(true);

    Cache<Long, String> fruitsCache = cacheManager.getCache("fruitsCache", Long.class, String.class);   // <3>
    fruitsCache.put(1L, "apple");
    fruitsCache.put(2L, "mango");
    fruitsCache.put(3L, "orange");   // <4>
    assertThat(fruitsCache.get(1L), is("apple"));   // <5>
    
    cacheManager.close();   // <6>
    cacheManager.init();    // <7>
    fruitsCache = cacheManager.getCache("fruitsCache", Long.class, String.class);   // <8>
    assertThat(fruitsCache.get(1L), is("apple"));   // <9>
    // end::transientSerializerBadSample[]
  }

  @Test
  public void testPersistentSerializer() throws Exception {
    // tag::persistentSerializerGoodSample[]
    CacheConfiguration<Long, String> cacheConfig =
        CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Long.class, String.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder()
                .heap(10, EntryUnit.ENTRIES).disk(10, MemoryUnit.MB, true))
            .withValueSerializer(SimplePersistentStringSerializer.class)   // <1>
            .build();

    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .with(new CacheManagerPersistenceConfiguration(new File(PERSISTENCE_PATH)))
        .withCache("fruitsCache", cacheConfig)
        .build(true);

    Cache<Long, String> fruitsCache = cacheManager.getCache("fruitsCache", Long.class, String.class);
    fruitsCache.put(1L, "apple");
    fruitsCache.put(2L, "mango");
    fruitsCache.put(3L, "orange");
    assertThat(fruitsCache.get(1L), is("apple"));

    cacheManager.close();
    cacheManager.init();
    fruitsCache = cacheManager.getCache("fruitsCache", Long.class, String.class);
    assertThat(fruitsCache.get(1L), is("apple"));
    // end::persistentSerializerGoodSample[]
  }
  
  @Test
  public void testKryoSerializer() throws Exception {
    // tag::thirdPartySerializer[]
    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    CacheConfiguration<Long, Employee> cacheConfig =
        CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, Employee.class, ResourcePoolsBuilder.heap(10))
            .withValueSerializer(KryoSerializer.class)  // <1>
            .build();

    Cache<Long, Employee> employeeCache = cacheManager.createCache("employeeCache", cacheConfig);
    Employee emp =  new Employee(1234, "foo", 23, new Description("bar", 879));
    employeeCache.put(1L, emp);
    assertThat(employeeCache.get(1L), is(emp));
    // end::thirdPartySerializer[]
  }

  @Test
  public void testTransientKryoSerializer() throws Exception {
    // tag::transientKryoSerializer[]
    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    CacheConfiguration<Long, Employee> cacheConfig =
        CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, Employee.class, ResourcePoolsBuilder.heap(10))
            .withValueSerializer(TransientKryoSerializer.class)
            .build();

    Cache<Long, Employee> employeeCache = cacheManager.createCache("employeeCache", cacheConfig);
    Employee emp =  new Employee(1234, "foo", 23, new Description("bar", 879));
    employeeCache.put(1L, emp);
    assertThat(employeeCache.get(1L), is(emp));
    // end::transientKryoSerializer[]
  }

  @Test
  public void testPersistentKryoSerializer() throws Exception {
    // tag::persistentKryoSerializer[]
    CacheConfiguration<Long, Employee> cacheConfig =
        CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Long.class, Employee.class,
            ResourcePoolsBuilder.newResourcePoolsBuilder()
                .heap(10, EntryUnit.ENTRIES).offheap(5, MemoryUnit.MB).disk(10, MemoryUnit.MB, true))
            .withValueSerializer(PersistentKryoSerializer.class)
            .build();

    CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
        .with(new CacheManagerPersistenceConfiguration(new File(PERSISTENCE_PATH)))
        .withCache("employeeCache", cacheConfig)
        .build(true);

    Cache<Long, Employee> employeeCache = cacheManager.getCache("employeeCache", Long.class, Employee.class);
    Employee emp =  new Employee(1234, "foo", 23, new Description("bar", 879));
    employeeCache.put(1L, emp);
    assertThat(employeeCache.get(1L), is(emp));

    cacheManager.close();
    cacheManager.init();
    employeeCache = cacheManager.getCache("employeeCache", Long.class, Employee.class);
    assertThat(employeeCache.get(1L), is(emp));
    // end::persistentKryoSerializer[]
  }

}
