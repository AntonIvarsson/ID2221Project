---
title: "Project Report"
subtitle: "ID2221"
author: "Gustave Rousselet, Anton Ivarsson, Jacob Stachowicz"
---

# Introduction

In this project, we implemented an end-to-end data processing pipeline to mimic that of a production system. The background for the project was a ficticious system where a Swedish government agency wanted to analyze incoming high velocity streams of social media data in order to then understand where possible COVID-19 infections were clustering in Stockholm. However, due to the sheer size of the incoming data, the agenecy can't store the data directly, but must instead aggregate the data over time to a more compact format which will then be contiuously aggregated and updated for real time analysis. 

Our approach was to build a data producer using Scala, which would produce social media messages tagged with a location which given random probability would contain COVID-19 keyword indicators. Then, these messages are piped to Kafka which acts as a message broker for the consumer program. The consumer program then consumes the messages from Kafka using Spark Streaming. The messages are the filtered and transformed to a compact format grid index (representing a point in Stockholm) and the counts of messages are aggregated over time. These counts are written to Cassandra and then finally used in the dashboard to display the counts of messages in different area in Stockholm. 

# Method

## Producer (Scala + Kafka)

## Consumer (Spark Streaming + Cassandra)

The consumer works by reading the incoming data stream of messages from Kafka. The messages received are of the format `<LONGITUDE>, <LATITUDE>, <MESSAGE>`. This incoming message is then split up using the `map` function to get the coordinate and message string. Then, a filtering function processes the stream and removes messages which do not contain a COVID keyword using the `filter` function. Then, each message is mapped to it's nearest grid cell on a 10x10 grid of Stockholm using the `map` function, each grid cell is given a unique index from 0-99. Now the stream is of `(grid_cell, 1)` so each element represents a grid cell and a count of a COVID flagged message in that grid cell. The `mapWithState` function is then used in combination with a state which is a `Map[Int, Int]` which maps each grid cell to a count of COVID flagged messages in the grid cell. `mapWithState` then aggregates the incoming data stream information to increment the counts for the grid cells. The consumer then writes this continuously updated state aggregation to Cassandra.

## Dashboard (Jupyter Notebook)

# Conclusion 

The data pipeline that we built was able to handle an incoming data stream with high volume and velocity (100s of messages per second) and able to aggregate this incoming stream into a compact format for further analysis with little data storage overhead. The project could be further improved by testing the pipeline on a distributed cluster and seeing if the system can be further stress tested with data incoming from several different producers. 