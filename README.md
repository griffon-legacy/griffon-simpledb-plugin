
Simpledb support
----------------

Plugin page: [http://artifacts.griffon-framework.org/plugin/simpledb](http://artifacts.griffon-framework.org/plugin/simpledb)


The Simpledb plugin enables lightweight access to [Amazon's Simpledb][1] databases.
This plugin does NOT provide domain classes nor dynamic finders like GORM does.

Usage
-----
Upon installation the plugin will generate the following artifacts in `$appdir/griffon-app/conf`:

 * SimpledbConfig.groovy - contains the database definitions.
 * BootstrapSimpledb.groovy - defines init/destroy hooks for data to be manipulated during app startup/shutdown.

A new dynamic method named `withSimpledb` will be injected into all controllers,
giving you access to a `com.amazonaws.services.simpledb.AmazonSimpleDB` object, with which you'll be able
to make calls to the database. Remember to make all database calls off the EDT
otherwise your application may appear unresponsive when doing long computations
inside the EDT.

This method is aware of multiple databases. If no databaseName is specified when calling
it then the default database will be selected. Here are two example usages, the first
queries against the default database while the second queries a database whose name has
been configured as 'internal'

    package sample
    class SampleController {
        def queryAllDatabases = {
            withSimpledb { databaseName, client -> ... }
            withSimpledb('internal') { databaseName, client -> ... }
        }
    }

This method is also accessible to any component through the singleton `griffon.plugins.simpledb.SimpledbConnector`.
You can inject these methods to non-artifacts via metaclasses. Simply grab hold of a particular metaclass and call
`SimpledbEnhancer.enhance(metaClassInstance, simpledbProviderInstance)`.

Configuration
-------------
### Dynamic method injection

The `withSimpledb()` dynamic method will be added to controllers by default. You can
change this setting by adding a configuration flag in `griffon-app/conf/Config.groovy`

    griffon.simpledb.injectInto = ['controller', 'service']

### Events

The following events will be triggered by this addon

 * SimpledbConnectStart[config, databaseName] - triggered before connecting to the database
 * SimpledbConnectEnd[databaseName, client] - triggered after connecting to the database
 * SimpledbDisconnectStart[config, databaseName, client] - triggered before disconnecting from the database
 * SimpledbDisconnectEnd[config, databaseName] - triggered after disconnecting from the database

### Multiple Stores

The config file `SimpledbConfig.groovy` defines a default client block. As the name
implies this is the client used by default, however you can configure named clients
by adding a new config block. For example connecting to a database whose name is 'internal'
can be done in this way

    clients {
        internal {
            credentials {
                accessKey = '*****'
                secretKey = '*****'
            }
        }
    }

This block can be used inside the `environments()` block in the same way as the
default client block is used.

### Credentials

Access and secret keys can be obtained from [https://aws-portal.amazon.com/gp/aws/securityCredentials][2] after you have successfully
signed in for usage of the Simpledb services provided by Amazon.

### Example

A trivial sample application can be found at [https://github.com/aalmiray/griffon_sample_apps/tree/master/persistence/simpledb][3]

Testing
-------
The `withSimpledb()` dynamic method will not be automatically injected during unit testing, because addons are simply not initialized
for this kind of tests. However you can use `SimpledbEnhancer.enhance(metaClassInstance, simpledbProviderInstance)` where 
`simpledbProviderInstance` is of type `griffon.plugins.simpledb.SimpledbProvider`. The contract for this interface looks like this

    public interface SimpledbProvider {
        Object withSimpledb(Closure closure);
        Object withSimpledb(String clientName, Closure closure);
        <T> T withSimpledb(CallableWithArgs<T> callable);
        <T> T withSimpledb(String clientName, CallableWithArgs<T> callable);
    }

It's up to you define how these methods need to be implemented for your tests. For example, here's an implementation that never
fails regardless of the arguments it receives

    class MySimpledbProvider implements SimpledbProvider {
        Object withSimpledb(String clientName = 'default', Closure closure) { null }
        public <T> T withSimpledb(String clientName = 'default', CallableWithArgs<T> callable) { null }
    }

This implementation may be used in the following way

    class MyServiceTests extends GriffonUnitTestCase {
        void testSmokeAndMirrors() {
            MyService service = new MyService()
            SimpledbEnhancer.enhance(service.metaClass, new MySimpledbProvider())
            // exercise service methods
        }
    }


[1]: http://aws.amazon.com/simpledb/
[2]: https://aws-portal.amazon.com/gp/aws/securityCredentials
[3]: https://github.com/aalmiray/griffon_sample_apps/tree/master/persistence/simpledb

