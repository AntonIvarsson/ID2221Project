---
title: "Project Report"
subtitle: "ID2221"
author: "Gustave Rousselet, Anton Ivarsson, Jacob Stachowicz"
---
 
# Introduction
 
In this project, we implemented an end-to-end data processing pipeline to mimic that of a production system. The background for the project was a fictitious system where a Swedish government agency wanted to analyze incoming high velocity streams of social media data in order to then understand where possible COVID-19 infections were clustering in Stockholm. However, due to the sheer size of the incoming data, the agency can't store the data directly, but must instead aggregate the data over time to a more compact format which will then be continuously aggregated and updated for real time analysis.
 
Our approach was to build a data producer using Scala, which would produce social media messages tagged with a location which given random probability would contain COVID-19 keyword indicators. Then, these messages are piped to Kafka which acts as a message broker for the consumer program. The consumer program then consumes the messages from Kafka using Spark Streaming. The messages are then filtered and transformed to a compact format grid index (representing a point in Stockholm) and the counts of messages are aggregated over time. These counts are written to Cassandra and then finally used in the dashboard to display the counts of messages in different areas in Stockholm.
 
An instruction on how to run this project is specified in the readme.md file.
 
# Method
 
## Dataset
 
We use two datasets in this project
1. A dataset containing 115 different twitter tweets from 2016 in the file dataset.csv. The twitter database used here is from 2016 (concerning the American election 2016).
2. A dataset with 15 words used for flagging messages containing words that can be associated with covid-19 in the file covidlist.csv. Examples of covid related words are "SARS CoV-2" and "dry cough".
 
## Producer (Scala + Kafka)
 
The producer reads in the two datasets (dataset.csv and covidlist.csv). 
it then creates messages by generating x and y coordinates together with a twitter tweet from the database.csv. Each message has a probability of 20% to be injected with a random covid related word from the covidlist.csv.
The generation of the x and y coordinates are created randomly following a gaussian distribution, where the mean is changed randomly each 2500th message and the variance is fixed. The 115 twitter messages used by the producer are continuously looped, so that the producer always has messages to work with.
 
A injected message could look like this:
<1932.9, 988.0, "original message original message **COVID-WORD** original message"> and a non-injected message without the COVID-WORD present.
 
Each message is created iteratively and continuously sent by the producer to the kafka queue. The messages sent to kafka is partitioned in the topic "covid",
(a topic that is subscribed by the consumer).
 
## Consumer (Spark Streaming + Cassandra)
 
The consumer works by reading the incoming data stream of messages from Kafka. The messages received are of the format `<LONGITUDE>, <LATITUDE>, <MESSAGE>`. This incoming message is then split up using the `map` function to get the coordinate and message string. Then, a filtering function processes the stream and removes messages which do not contain a COVID keyword using the `filter` function. Then, each message is mapped to it's nearest grid cell on a 100x100 grid of Stockholm using the `map` function, each grid cell is given a unique index from 0-99. Now the stream is of `(grid_cell, 1)` so each element represents a grid cell and a count of a COVID flagged message in that grid cell. The `mapWithState` function is then used in combination with a state which is a `Map[Int, Int]` which maps each grid cell to a count of COVID flagged messages in the grid cell. `mapWithState` then aggregates the incoming data stream information to increment the counts for the grid cells. The consumer then writes this continuously updated state aggregation to Cassandra.
 
## Dashboard (Jupyter Notebook)
 
The dashboard was created as a python program in the jupyter notebook format. Running the entire notebook visualizes the data stored in Cassandra by drawing clusters on a map of Stockholm. This is illustrated in the image below:
 
<p align="center"><img align = "center" src="images/map.png" width="80%"></p>
 
The notebook works by doing the following:
* Fetch data from Cassandra and save it to an array where each item is a tuple of grid index and count for said index.
* Calculate the sizes of the clusters to be drawn.
 * The size of each cluster proportional to the percentage of total covid tweets that are in that specific area.
* Draw the clusters on a map of Stockholm.
 
This dashboard could be used by Folkhälsomyndigheten to determine which areas of the city  require action to reduce the spread of the virus.
 
# Conclusion
 
The data pipeline that we built was able to handle an incoming data stream with high volume and velocity (100s of messages per second) and able to aggregate this incoming stream into a compact format for further analysis with little data storage overhead. The project could be further improved by testing the pipeline on a distributed cluster and seeing if the system can be further stress tested with data incoming from several different producers.

