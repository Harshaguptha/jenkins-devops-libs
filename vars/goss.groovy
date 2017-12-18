// vars/goss.groovy
def install(String version, String install_path = '/usr/bin/') {
  // check if current version already installed
  if (fileExists("${install_path}/goss")) {
    installed_version = sh(returnStdout: true, script: "${install_path}/goss --version").trim()
    if (installed_version =~ version) {
      echo "Goss version ${version} already installed at ${install_path}."
      return
    }
  }
  // otherwise download and install latest
  sh "curl -L https://github.com/aelsabbahy/goss/releases/download/v${version}/goss-linux-amd64 -o ${install_path}/goss"
  sh "chmod +rx ${install_path}/goss"
  echo "Goss successfully installed at ${install_path}/goss."
}

def server(body) {
  // evaluate the body block, and collect configuration into the object
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
  config.path = config.path == null ? 'goss' : config.path

  // create goss rest api endpoint
  try {
    cmd = "${config.path} -f ${config.format}"

    if (config.gossfile != null) {
      cmd += " -g ${config.gossfile}"
    }

    sh "${cmd} serve -e ${config.endpoint} -l ${config.port} &"
  }
  catch(Exception error) {
    echo 'Failure using goss serve.'
    throw error
  }
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
  config.port = config.port == null ? ':8080' : config.port
  config.path = config.path == null ? 'goss' : config.path

  // validate with goss
  try {
    cmd = "${config.path} -f ${config.format}"

    if (config.gossfile != null) {
      cmd += " -g ${config.gossfile}"
    }

    sh "${cmd} validate"
  }
  catch(Exception error) {
    echo 'Failure using goss validate.'
    throw error
  }
}

def validate_gossfile(String gossfile) {
  // ensure gossfile exists and then check yaml syntax
  if (fileExists(gossfile)) {
    try {
      readYaml(file: gossfile)
    }
    catch(Exception error) {
      echo 'Gossfile failed YAML validation.'
      throw error
    }
    echo "${gossfile} is valid YAML."
  }
  else {
    throw new Exception("Gossfile ${config.gossfile} does not exist!")
  }
}
