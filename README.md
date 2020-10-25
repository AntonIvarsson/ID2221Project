# ID2221Project

To run

### Build image and connect to container

```
docker build -t kafka-sparkstreaming-cassandra .
```

```
docker run -v `pwd`:/home/guest/host -p 4040:4040 -p 8888:8888 -p 23:22 -ti --privileged kafka-sparkstreaming-cassandra
```

### Run setup
```
sh /usr/bin/startup_script.sh
sh setenv.sh
```

### Open two additonal shells
Get the container id
```
docker ps
```

Do twice
```
docker exec -it <CONTAINER_ID> /bin/bash
```

Start consumer
```
cd consumer
sbt run
```

Start producer
```
cd producer
sbt run
```

Start notebook
```
cd notebooks
notebook --NotebookApp.iopub_data_rate_limit=1.0e10 --allow-root
```


