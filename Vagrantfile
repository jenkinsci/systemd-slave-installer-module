# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  config.vm.box = "fedora18"
  config.vm.box_url = "http://puppet-vagrant-boxes.puppetlabs.com/fedora-18-x64-vbox4210-nocm.box"

  config.vm.provider :virtualbox do |vb|
    # Don't boot with headless mode
    vb.gui = true
  end

  config.vm.provision "shell", path: "src/provision.sh"
end
