### The COVID-19 visualization/dashboard

The visualization was created as a python program in the jupyter notebook format. Running the entire notebook visualizes the data stored in Cassandra by drawing clusters on a map of Stockholm. The notebook does the following: 

* Fetch data from Cassandra, and save it to an array where each item is a tuple of grid index and count for said index.
* Calculate the sizes of the clusters to be drawn. 
  * This is done by making the size of each cluster proportional to the percentage of total covid tweets that are in that specific area. 
* Draw the clusters on the map of Stockholm. 

The idea is that this dashboard could be used by Folkhälsomyndigheten to determine which areas of the city  require action to reduce the spread of the virus.