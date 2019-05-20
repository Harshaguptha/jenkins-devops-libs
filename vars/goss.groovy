// vars/goss.groovy
import devops.common.utils

def install(String version, String install_path = '/usr/bin/') {
  assert fileExists(install_path) : "The desired installation path at ${install_path} does not exist."

  // check if current version already installed
  if (fileExists("${install_path}/goss")) {
    installed_version = sh(returnStdout: true, script: "${install_path}/goss --version").trim()
    if (installed_version ==~ version) {
      print "Goss version ${version} already installed at ${install_path}."
      return
    }
  }
  // otherwise download and install specified version
  new utils().download_file("https://github.com/aelsabbahy/goss/releases/download/v${version}/goss-linux-amd64", "${install_path}/goss")
  sh "chmod +rx ${install_path}/goss"
  print "Goss successfully installed at ${install_path}/goss."
}

def install_dgoss(String version, String install_path = '/usr/bin/') {
  assert fileExists(install_path) : "The desired installation path at ${install_path} does not exist."

  // check if current version already installed
  if (fileExists("${install_path}/dgoss") && fileExists("${install_path}/goss")) {
    installed_version = sh(returnStdout: true, script: "${install_path}/goss --version").trim()
    if (installed_version ==~ version) {
      print "Dgoss version ${version} already installed at ${install_path}."
      return
    }
  }
  assert (!(fileExists("${install_path}/dgoss"))) : 'Dgoss is installed but goss is not. Dgoss execution requires goss.'

  // otherwise download and install specified version
  new utils().download_file("https://raw.githubusercontent.com/aelsabbahy/goss/v${version}/extras/dgoss/dgoss", "${install_path}/dgoss")
  sh "chmod +rx ${install_path}/dgoss"
  print "Dgoss successfully installed at ${install_path}/dgoss."
}

def server(body) {
  // evaluate the body block and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  if ((config.gossfile != null) && (!fileExists(config.gossfile))) {
    throw new Exception("Gossfile ${config.gossfile} does not exist!")
  }
  config.endpoint = config.endpoint == null ? '/healthz' : config.endpoint
  config.format = config.format == null ? 'rspecish' : config.format
  config.port = config.port == null ? ':8080' : config.port
  config.bin = config.bin == null ? 'goss' : config.bin

  // create goss rest api endpoint
  try {
    cmd = "${config.bin}"

    // check for optional inputs
    if (config.vars != null) {
      assert fileExists(config.vars) : "The vars file ${config.vars} does not exist!"

      cmd += " --vars ${config.vars}"
    }
    if (config.gossfile != null) {
      cmd += " -g ${config.gossfile}"
    }

    sh "nohup ${cmd} serve -f ${config.format} -e ${config.endpoint} -l ${config.port} &"
  }
  catch(Exception error) {
    print 'Failure using goss serve.'
    throw error
  }
  print 'Goss endpoint created successfully.'
}

def validate(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  if ((config.gossfile != null) && (!fileExists(config.gossfile))) {
    throw new Exception("Gossfile ${config.gossfile} does not exist!")
  }
  config.format = config.format == null ? 'rspecish' : config.format
  config.bin = config.bin == null ? 'goss' : config.bin

  // validate with goss
  try {
    cmd = "${config.bin}"

    // check for optional inputs
    if (config.vars != null) {
      assert fileExists(config.vars) : "The vars file ${config.vars} does not exist!"

      cmd += " --vars ${config.vars}"
    }
    if (config.gossfile != null) {
      cmd += " -g ${config.gossfile}"
    }

    sh "${cmd} validate -f ${config.format}"
  }
  catch(Exception error) {
    print 'Failure using goss validate.'
    throw error
  }
  print 'Goss validate command was successful.'
}

def validate_docker(body) {
  // evaluate the body block, and collect configuration into the object
  def config = [:]
  body.resolveStrategy = Closure.DELEGATE_FIRST
  body.delegate = config
  body()

  // input checking
  assert config.image != null : 'The required image parameter was not set.'

  config.bin = config.bin == null ? 'dgoss' : config.bin

  // run with dgoss
  try {
    cmd = "${config.bin} run"
    // check for optional inputs
    if (config.flags != null) {
      assert (config.flags instanceof String[]) : 'The flags parameter must be an array of strings.'

      config.flags.each() { flag ->
        cmd += " -e ${flag}"
      }
    }

    sh "${cmd} ${config.image}"
  }
  catch(Exception error) {
    print 'Failure using dgoss run.'
    throw error
  }
  print 'Dgoss run command was successful.'
}

def validate_gossfile(String gossfile) {
  // ensure gossfile exists and then check yaml syntax
  assert fileExists(gossfile) : "Gossfile ${gossfile} does not exist!"

  try {
    readYaml(file: gossfile)
  }
  catch(Exception error) {
    print 'Gossfile failed YAML validation.'
    throw error
  }
  print "${gossfile} is valid YAML."
}
