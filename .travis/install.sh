#!/usr/bin/env bash

date

# maven
echo "Resolving maven dependencies and plugins"
MAVEN_OPTS="-Dorg.slf4j.simpleLogger.defaultLogLevel=warn $MAVEN_OPTS" mvn dependency:resolve dependency:resolve-plugins --batch-mode -e -q

# front
for i in component-tools-webapp component-starter-server component-tools; do
    cd $i
        mvn frontend:install-node-and-yarn@install-node-and-yarn frontend:yarn@yarn-install --batch-mode -e -q
    cd -
done

# studio (p2)
cd component-studio-integration
    MAVEN_OPTS="-Dtalend.component.kit.build.studio.m2.forceupdate=true $MAVEN_OPTS" mvn gplus:execute@setup-deps --batch-mode -e -q
cd -

# documentation - we got some issues on travis so this is a sanity check
cd documentation
    mvn dependency:unpack@unpack-api-javadoc --batch-mode -e -q
cd -
