# Packer

Interacts with Packer.

### Dependencies

- unzip package (`install`)
- pipeline-utility-steps plugin (`install`)
- devops.common.utils

### packer.build {}
Uses Packer to build an artifact from a template.

```groovy
packer.build{
  bin = '/usr/bin/packer' // optional location of packer install
  template = '/path/to/template.json' // location of packer template
  var_file = '/path/to/variables.json' // optional location of variables file
}
```

### packer.install {}
Locally installs a specific version of Packer.

```groovy
packer.install {
  install_path = '/usr/bin' // optional location to install packer
  platform = 'linux_amd64' // platform where packer will be installed
  version = '1.1.3' // version of packer to install
}
```

### packer.validate {}
Uses Packer to validate a build template.

```groovy
packer.validate{
  bin = '/usr/bin/packer' // optional location of packer install
  template = '/path/to/template.json' // location of packer template
  var_file = '/path/to/variables.json' // optional location of variables file
}
```
