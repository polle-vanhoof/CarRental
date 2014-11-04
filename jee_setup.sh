#Voor mij werkt het op deze manier:
#$ set CLASSPATH assignment1/CarRental/build
#$ full_path_to/glassfish/bin/appclient -xml full_path_to/glassfish/domains/dissys/config/glassfish-acc.xml -client assignment1/CarRental/build/CarRental-client.jar 

#!/bin/bash

export J2EE_HOME=$(echo /localhost/packages/ds/glassfish-*)
nbhome=$(echo /localhost/packages/ds/netbeans-*)
export PATH=$J2EE_HOME/glassfish/bin/:$nbhome/bin:$PATH
