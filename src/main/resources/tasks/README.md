
# TASK 1

Load sensor data into store.
 

# TASK 2

Scan time series data of a few meters:
    scan meter data for 180 days of 1-10 meters

Execute scan request in multi-thread (maximum 1 scan 1 thread).

Check throughout is improved by running scan requests in parallel:
	Number of threads: 1 to 100	 →  Throughput: 50,000 to 350,000 records/sec (7x)

Output:
Graph 1- read time	Read time vs Number of threads
Graph 2- throughput	Records per second vs Number of threads

# TASK 3

Get the latest data of many meters - with multi thread
Get the latest time e.g. 30 minutes data of 100 to 1000 meters

Scan request can not be applied to these data.
Requests are executed in multi-thread
Batch execution of multiple “Get” request by one “batch” request.

Check throughout is improved by running Get requests in parallel:
	Number of threads: 1 to 100	 →  Throughput: 1,000 to 7,500 records/sec (7.5x)

Output:
Graph 1- read time 	Read time vs Number of threads
Graph 2- throughput	Records per second vs Number of threads


# TASK 4

Analyze the data from smart meters to grasp the load of equipment 

suppose: 

Electricity supplier company name = XYZ
Number of Substation = 100
Number of distribution lines per substation = 5

Area name = ABC
Number of distribution lines = 20 


------------------------------------------------------------------------

template :

  select ?fr ?last (min(?dist) as ?mindist)
                ((?bday - xsd:dateTime("1970-01-01T00:00:00.000+00:00")) * 1000 as ?birthday)
                ((?since - xsd:dateTime("1970-01-01T00:00:00.000+00:00")) * 1000 as ?creationDate)
  	      ?gen ?browser ?locationIP ?emails ?lngs ?based ?studyAt ?workAt #Q1
  {
      ?fr a snvoc:Person . ?fr snvoc:firstName "%firstName%" .
      optional { ?fr snvoc:lastName ?last } .
      optional { ?fr snvoc:birthday ?bday } .
      ?fr snvoc:creationDate ?since . ?fr snvoc:gender ?gen . ?fr snvoc:locationIP ?locationIP .
      ?fr snvoc:browserUsed ?browser .
      {
        { select distinct ?fr (1 as ?dist)
          where {
            sn:pers%personId% snvoc:knows ?fr.
          }
        }
        union
        { select distinct ?fr (2 as ?dist)
          where {
            sn:pers%personId% snvoc:knows ?fr2. ?fr2 snvoc:knows ?fr. filter (?fr != sn:pers%personId%).
          }
        }
        union
        { select distinct ?fr (3 as ?dist)
          where {
            sn:pers%personId% snvoc:knows ?fr2. ?fr2 snvoc:knows ?fr3. ?fr3 snvoc:knows ?fr. filter (?fr != sn:pers%personId%).
          }
        } .
      } .
      ?fr snvoc:isLocatedIn ?basedURI . ?basedURI foaf:name ?based .
      {
        select ?fr (group_concat (?email; separator = ", ") as ?emails)
        where {
              ?fr snvoc:email ?email .
        }
        group by ?fr
      } .
      {
        select ?fr (group_concat (?lng; separator = ", ") as ?lngs)
        where {
              ?fr snvoc:speaks ?lng .
        }
        group by ?fr
      } .
      optional {
        select ?fr (group_concat ( concat (?o_name, " ", ?year, " ", ?o_country); separator = ", ") as ?studyAt)
        where {
              ?fr snvoc:studyAt ?w .
              ?w snvoc:classYear ?year .
              ?w snvoc:hasOrganisation ?org .
              ?org snvoc:isLocatedIn ?o_countryURI .
              ?o_countryURI foaf:name ?o_country .
              ?org foaf:name ?o_name .
        }
        group by ?fr
      } .
      optional {
        select ?fr (group_concat ( concat (?o_name, " ", ?year, " ", ?o_country); separator = ", ") as ?workAt)
        where {
              ?fr snvoc:workAt ?w .
              ?w snvoc:workFrom ?year .
              ?w snvoc:hasOrganisation ?org .
              ?org snvoc:isLocatedIn ?o_countryURI .
              ?o_countryURI foaf:name ?o_country .
              ?org foaf:name ?o_name .
        }
        group by ?fr
      } .
  }
  group by ?fr ?last ?bday ?since ?gen ?browser ?locationIP ?emails ?lngs ?based ?studyAt ?workAt
  order by ?mindist ?last ?fr
  limit %limit%

--------

select ?con ((?dt - xsd:dateTime("1970-01-01T00:00:00.000+00:00")) * 1000 as ?date)
where {
  ?post snvoc:id %messageId% .
  { {?post snvoc:content ?con } union { ?post snvoc:imageFile ?con } union { ?post snvoc:gifFile ?con }} .
  ?post snvoc:creationDate ?dt .
}

