#!/bin/bash
#
# Imports knir 3rd party libraries into Nexus
#
# Author: FracPete (fracpete at waikato dot ac dot nz)
# Version: $Revision: 1945 $

HOST=https://adams.cms.waikato.ac.nz
REPO=adams-spectral-thirdparty
REPO_URL=$HOST/nexus/content/repositories/$REPO

LIB_DIR=./

GROUP=net.sourceforge.jspecview
mvn deploy:deploy-file -DgroupId=$GROUP \
  -DartifactId=jspecview \
  -Dversion=2011-10-06 \
  -Dpackaging=jar \
  -Dfile=$LIB_DIR/jspecview-2011-10-06.jar \
  -Dsources=$LIB_DIR/jspecview-2011-10-06-sources.jar \
  -DgeneratePom.description="JSpecView application for reading JCamp-DX, AniML and CML files - http://sourceforge.net/projects/jspecview/." \
  -DrepositoryId=$REPO \
  -Durl=$REPO_URL
mvn deploy:deploy-file -DgroupId=$GROUP \
  -DartifactId=jspecview \
  -Dversion=2012-07-25 \
  -Dpackaging=jar \
  -Dfile=$LIB_DIR/jspecview-2012-07-25.jar \
  -Dsources=$LIB_DIR/jspecview-2012-07-25-sources.jar \
  -DgeneratePom.description="JSpecView application for reading JCamp-DX, AniML and CML files - http://sourceforge.net/projects/jspecview/." \
  -DrepositoryId=$REPO \
  -Durl=$REPO_URL
mvn deploy:deploy-file -DgroupId=$GROUP \
  -DartifactId=jspecview-lib \
  -Dversion=2012-07-25 \
  -Dpackaging=jar \
  -Dfile=$LIB_DIR/jspecview-lib-2012-07-25.jar \
  -Dsources=$LIB_DIR/jspecview-lib-2012-07-25-sources.jar \
  -DgeneratePom.description="JSpecView library for reading JCamp-DX, AniML and CML files - http://sourceforge.net/projects/jspecview/." \
  -DrepositoryId=$REPO \
  -Durl=$REPO_URL

GROUP=net.sourceforge.jcamp-dx
mvn deploy:deploy-file -DgroupId=$GROUP \
  -DartifactId=jcampdx-lib \
  -Dversion=0.9.1 \
  -Dpackaging=jar \
  -Dfile=$LIB_DIR/jcampdx-lib-0.9.1.jar \
  -Dsources=$LIB_DIR/jcampdx-lib-0.9.1-sources.jar \
  -Djavadoc=$LIB_DIR/jcampdx-lib-0.9.1-javadoc.jar \
  -DgeneratePom.description="The JCAMP-DX project is the reference implemention of the IUPAC JCAMP-DX spectroscopy data standard (https://sourceforge.net/projects/jcamp-dx/)." \
  -DrepositoryId=$REPO \
  -Durl=$REPO_URL

