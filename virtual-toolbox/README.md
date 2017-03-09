# New joiner (or how to set up from scratch - macOS 10.12.x)

1. Install [Homebrew](https://brew.sh/) package manager for macOS.
1. Install ansible with hombrew: `brew install ansible`


> [Note](http://docs.ansible.com/ansible/intro_installation.html#control-machine-requirements)
> As of version 2.0, Ansible uses a few more file handles to manage its forks. Mac OS X by default is configured for a small amount of file handles, so if you want to use 15 or more forks you’ll need to raise the ulimit with sudo launchctl limit maxfiles unlimited. This command can also fix any “Too many open files” error.
