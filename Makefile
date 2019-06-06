install-deps:
	mvn validate

test-benchmark-noTest:
	mvn -Dtest=BenchmarkTest#checkHealth test -DfailIfNoTests=false

test-benchmark:
	mvn -Dtest=BenchmarkTest#checkHealth test

package:
	mvn -DskipTests -DincludeDeps=true package


build-images:
	mvn -Dtest=BenchmarkTest#buildImages surefire:test

test-dockerized-benchmark:
	mvn -Dtest=BenchmarkTest#checkHealthDockerized test


dockerize:
	docker build -f ./docker/benchmark-controller.docker -t git.project-hobbit.eu/bushran/iotpipe-benchmark/benchmark-controller .
	docker build -f ./docker/data-generator.docker -t git.project-hobbit.eu/bushran/iotpipe-benchmark/datagen .
	docker build -f ./docker/task-generator.docker -t git.project-hobbit.eu/bushran/iotpipe-benchmark/taskgen .
	docker build -f ./docker/carbon-graphite.docker -t git.project-hobbit.eu/bushran/iotpipe-system/carbon-graphite .
	docker build -f ./docker/kafka.docker -t git.project-hobbit.eu/bushran/iotpipe-system/kafka .
	docker build -f ./docker/storm-nimbus.docker -t git.project-hobbit.eu/bushran/iotpipe-system/storm-nimbus .
	docker build -f ./docker/storm-supervisor.docker -t git.project-hobbit.eu/bushran/iotpipe-system/storm-supervisor .
	docker build -f ./docker/zookeeper.docker -t git.project-hobbit.eu/bushran/iotpipe-system/zookeeper .
	docker build -f ./docker/evaluation-module.docker -t git.project-hobbit.eu/bushran/iotpipe-benchmark/eval-module .
	docker build -f ./docker/system-adapter.docker -t git.project-hobbit.eu/bushran/iotpipe-system/system-adapter .
	docker build -f ./docker/evaluation-storage.docker -t git.project-hobbit.eu/bushran/iotpipe-benchmark/eval-storage .


dockerize-bm:
	docker build -f ./docker/benchmark-controller.docker -t git.project-hobbit.eu:4567/bushran/iotpipe-benchmark/benchmark-controller .
	docker build -f ./docker/data-generator.docker -t git.project-hobbit.eu:4567/bushran/iotpipe-benchmark/datagen .
	docker build -f ./docker/evaluation-module.docker -t git.project-hobbit.eu:4567/bushran/iotpipe-benchmark/eval-module .
	docker build -f ./docker/evaluation-storage.docker -t git.project-hobbit.eu:4567/bushran/iotpipe-benchmark/eval-storage .
	docker build -f ./docker/task-generator.docker -t git.project-hobbit.eu:4567/bushran/iotpipe-benchmark/taskgen .

dockerize-sys:
	docker build -f ./docker/carbon-graphite.docker -t git.project-hobbit.eu:4567/bushran/iotpipe-system/carbon-graphite .
	docker build -f ./docker/storm-nimbus.docker -t git.project-hobbit.eu:4567/bushran/iotpipe-system/storm-nimbus .
	docker build -f ./docker/storm-supervisor.docker -t git.project-hobbit.eu:4567/bushran/iotpipe-system/storm-supervisor .
	docker build -f ./docker/zookeeper.docker -t git.project-hobbit.eu:4567/bushran/iotpipe-system/zookeeper .
	docker build -f ./docker/kafka.docker -t git.project-hobbit.eu:4567/bushran/iotpipe-system/kafka .
	docker build -f ./docker/system-adapter.docker -t git.project-hobbit.eu:4567/bushran/iotpipe-system/system-adapter .


dockerize-storm:
	docker build -f ./docker/storm-nimbus.docker -t git.project-hobbit.eu:4567/bushran/iotpipe-system/storm-nimbus .
	docker build -f ./docker/storm-supervisor.docker -t git.project-hobbit.eu:4567/bushran/iotpipe-system/storm-supervisor .
	docker build -f ./docker/storm-ui.docker -t git.project-hobbit.eu:4567/bushran/iotpipe-system/storm-ui .

rm-storm:
	docker image rm  git.project-hobbit.eu:4567/bushran/iotpipe-system/storm-nimbus .
	docker image rm  git.project-hobbit.eu:4567/bushran/iotpipe-system/storm-supervisor .
	docker image rm  git.project-hobbit.eu:4567/bushran/iotpipe-system/storm-ui .


push-bm:
	docker push git.project-hobbit.eu:4567/bushran/iotpipe-benchmark/benchmark-controller
	docker push git.project-hobbit.eu:4567/bushran/iotpipe-benchmark/datagen
	docker push git.project-hobbit.eu:4567/bushran/iotpipe-benchmark/taskgen
	docker push git.project-hobbit.eu:4567/bushran/iotpipe-benchmark/eval-storage
	docker push git.project-hobbit.eu:4567/bushran/iotpipe-benchmark/eval-module

push-sys:
	docker push git.project-hobbit.eu:4567/bushran/iotpipe-system/carbon-graphite
	docker push  git.project-hobbit.eu:4567/bushran/iotpipe-system/kafka
	docker push  git.project-hobbit.eu:4567/bushran/iotpipe-system/storm-nimbus
	docker push  git.project-hobbit.eu:4567/bushran/iotpipe-system/storm-supervisor
	docker push   git.project-hobbit.eu:4567/bushran/iotpipe-system/zookeeper
	docker push   git.project-hobbit.eu:4567/bushran/iotpipe-system/system-adapter





