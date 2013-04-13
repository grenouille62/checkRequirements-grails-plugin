import org.apache.tools.ant.BuildException

includeTargets << grailsScript('_GrailsInit')

target('checkRequirements': 'Run CheckRequirements') {
    runCheckRequirements()
}

private void runCheckRequirements() {
    ant.taskdef(name: 'checkRequirements', classname: 'checkRequirements.ant.task.CheckRequirementsTask')


    def configClassName = getBindingValueOrDefault('configClassname', "BuildConfig")
    def config = loadConfig(configClassName)


    List includes = configureIncludes(config.checkRequirements)
    boolean systemExitOnBuildException = false

    //configureCodeNarcPropertiesFile(config)

    println "Running CheckRequirements ..."

    try {
        ant.mkdir(dir: "target/reports")
        ant.checkRequirements(requirementssetfile: config.checkRequirements.requirementssetfile,
                outputdir: config.checkRequirements.outputdir,
                outputfile: config.checkRequirements.outputfile,
                outputstyle: config.checkRequirements.outputstyle,
                inputType: config.checkRequirements.inputType) {

            fileset(dir: '.', includes: includes.join(','))
        }
    }
    catch (BuildException e) {
        if (systemExitOnBuildException) {
            println "FAILED -- ${e.message}"
            System.exit(1)
        } else {
            throw e
        }
    }

    println "CheckRequirement terminé : Rapport " + config.checkRequirements.outputdir + config
            .checkRequirements.outputfile + "." + config.checkRequirements.outputstyle + " généré"
}


private ConfigObject loadConfig(String className) {
    def classLoader = Thread.currentThread().contextClassLoader
    classLoader.addURL(new File(classesDirPath).toURL())

    try {
        return new ConfigSlurper().parse(new File('grails-app/conf/BuildConfig.groovy').toURL())
    }
    catch (ClassNotFoundException e) {
        return new ConfigObject()
    }
}

private getBindingValueOrDefault(String varName, Object defaultValue) {
    def variables = getBinding().getVariables()
    return variables.containsKey(varName) ? getProperty(varName) : defaultValue
}



private boolean getConfigBoolean(config, String name, boolean defaultValue = true) {
    def value = config[name]
    return value instanceof Boolean ? value : defaultValue
}

private List configureIncludes(config) {
    List includes = []

    if (getConfigBoolean(config, 'processSrcGroovy')) {
        includes << 'src/groovy/**/*.groovy'
    }

    if (getConfigBoolean(config, 'processSrcGroovy')) {
        includes << 'src/java/**/*.java'
    }

    if (getConfigBoolean(config, 'processControllers')) {
        includes << 'grails-app/controllers/**/*.groovy'
    }

    if (getConfigBoolean(config, 'processDomain')) {
        includes << 'grails-app/domain/**/*.groovy'
    }

    if (getConfigBoolean(config, 'processServices')) {
        includes << 'grails-app/services/**/*.groovy'
    }

    if (getConfigBoolean(config, 'processTaglib')) {
        includes << 'grails-app/taglib/**/*.groovy'
    }

    if (getConfigBoolean(config, 'processUtils')) {
        includes << 'grails-app/utils/**/*.groovy'
    }

    if (getConfigBoolean(config, 'processTestUnit')) {
        includes << 'test/unit/**/*.groovy'
    }

    if (getConfigBoolean(config, 'processTestIntegration')) {
        includes << 'test/integration/**/*.groovy'
    }

    if (getConfigBoolean(config, 'processViews', false)) {
        includes << 'grails-app/views/**/*.gsp'
    }

    for (includeDir in config.extraIncludeDirs) {
        includes << "$includeDir/**/*.groovy"
    }

    return includes
}

try {
    // Required for Grails 1.3 and later
    setDefaultTarget("checkRequirements")
}
catch (MissingMethodException e) {
    // Ignore. Older versions of Groovy/Grails do not implement this method
}
