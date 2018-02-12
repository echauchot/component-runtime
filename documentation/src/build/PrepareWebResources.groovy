/**
 *  Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
import groovy.json.JsonOutput

// add js-search resource
def source = new File(project.basedir, 'src/main/frontend/node_modules/js-search/dist/umd/js-search.min.js')
def target = new File(project.build.directory, "${project.build.finalName}/js/js-search.min.js")
target.parentFile.mkdirs()
target.text = source.text
log.info("Imported js-search")

// populate index
class Document {
    String title
    String content
    String link
}

def readTitle = { file ->
    def firstLine = file.readLines()[0]
    if (firstLine.startsWith('=')) {
        return firstLine.substring(firstLine.lastIndexOf('=') + 1).trim()
    }
    return file.name.replace('.adoc', '').replace('-', ' ')
}
def readContent = { file ->
    file.readLines().findAll { !it.startsWith(':') && !it.startsWith('=') && !it.trim().isEmpty() }.join(' ').replace('`', '')
}

def sourceSearchJs = new File(project.basedir, "src/main/jbake/generated-assets/search.js")
def targetSearchJs = new File(project.build.directory, "${project.build.finalName}/js/search.js")
def index = []
// browse all pages and create an index for them
new File(project.basedir, 'src/main/jbake/content').listFiles()
        .findAll {
            !it.text.contains('include::') // we skip aggregator pages
        }
        .each { file ->
            // todo: pre tokenize?
            index.add(new Document(
                    title: readTitle(file),
                    content: readContent(file),
                    link: file.name.replace('.adoc', '.html')))
        }

targetSearchJs.text = sourceSearchJs.text.replace('${DOCUMENTS}', JsonOutput.toJson(index))
log.info("Generated js-search index")
