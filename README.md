# ID2221Project

To run

### Build image and connect to container

```
docker build -t kafka-sparkstreaming-cassandra .
```

```
docker run -p 4040:4040 -p 8888:8888 -p 23:22 -ti --privileged kafka-sparkstreaming-cassandra
```

### Run setup
```
sh /usr/bin/startup_script.sh
sh setenv.sh
```

### Open two additonal shells
Get the container id
```
docker container ls 
```

Open two terminal sessions (one for consumer one for producer)
```
docker exec -it <CONTAINER_ID> /bin/bash
```

Start consumer in first terminal session
```
cd consumer
sbt run
```

Start producer in second terminal session
```
cd producer
sbt run
```

Start notebook
```
cd notebooks
notebook --NotebookApp.iopub_data_rate_limit=1.0e10 --allow-root
```


