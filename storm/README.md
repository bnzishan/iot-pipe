# docker-storm
storm2




We created this image to ease deployment of Apache Storm. More related main.resources:

- Our [tutorial on how to use Storm with Docker Swarm](https://github.com/Baqend/tutorial-swarm-storm)
- the [baqend/storm on Docker Hub](https://hub.docker.com/r/baqend/storm/)
- the [the baqend/storm on GitHub](https://github.com/Baqend/docker-storm) 


The Docker image launches Apache Storm. By default, the `storm.local.hostname` property is set already, but you have to provide all other arguments that you would normally provide.

## No need for a `storm.yaml`!

Storm allows you to provide any property via command line arguments like this:

	bin/storm ui \
        -c storm.local.hostname="nimbus"
You are not restricted to strings, but can also provide integer lists:

	bin/storm supervisor \
        -c supervisor.slots.ports="[6700,6701]"
Or lists of strings:

	bin/storm supervisor \
        -c storm.zookeeper.servers="[\"zk1\",\"zk2\",\"zk3\"]"

You can look up [**all the parameters in the source code**](https://github.com/apache/storm/blob/master/storm-core/src/jvm/org/apache/storm/Config.java).


### Providing ZooKeeper the easy way

This image also lets you specify the ZooKeeper servers with a simple list (e.g. `"zk1,zk2,zk3"`) instead of the more verbose format Storm requires (e.g. `"[\"zk1\",\"zk2\",\"zk3\"]"`). 
So if your ZooKeeper cluster is made up of `zk1`, `zk2` and `zk3`, you can tell Storm to use this cluster by providing the following Docker environment variable:

	-e STORM_ZOOKEEPER_SERVERS="zk1,zk2,zk3"

Internally, this is passed to Storm as 

	-c storm.zookeeper.servers="[\"zk1\",\"zk2\",\"zk3\"]"


### Default Values

The baqend/storm Docker image uses the following default values that can be overridden by providing the corresponding arguments in the `run` statement (e.g. `-c nimbus.seeds="[\"nimbus1\",\"nimbus2\"]"`):

- `-c nimbus.seeds="[\"nimbus\"]"`: is also overridden by `nimbus.host` (depreciated since 1.0.0)
- `-c storm.local.hostname=<CONTAINER-ID>`: by default, the image assigns the value `nimbus` when `nimbus` is provided as argument; the docker container ID is used as hostname else
- `-c supervisor.slots.ports="[6700,6701,6702,6703]"`: only when the `supervisor` argument is provided

In consequence, only the ZooKeeper servers and the role argument (either `ui `, `nimbus` or `supervisor`) have to be provided in a `docker run` statement.


### Further Reading

For an example of how we use this image, also see our [tutorial on how to use Storm with Docker Swarm](https://github.com/Baqend/tutorial-swarm-storm).

