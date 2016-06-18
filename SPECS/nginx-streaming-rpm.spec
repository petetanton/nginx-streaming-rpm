Summary:     Streaming NGINX build
Name:           nginx-streaming
Version:        0.1
Release:        1
# License is a compulsory field so you have to put something there.
License:        none
Source:         %{name}.tar.gz
BuildArch:      noarch
BuildRoot:      %{_tmppath}/%{name}-build
# There's a list at /usr/share/doc/packages/rpm/GROUPS but you don't have to use one of them
Group:          System/Base
Vendor:         Pete Tanton

%description
Nginx with the rtmp streaming module

%prep
# the set up macro unpacks the source bundle and changes in to the represented by
# %{name} which in this case would be my_maintenance_scripts. So your source bundle
# needs to have a top level directory inside called my_maintenance _scripts
%setup -n %{name}

%build
# this section is empty for this example as we're not actually building anything

%install
cd $RPM_BUILD_ROOT
sudo make install
# create directories where the files will be located
mkdir -p $RPM_BUILD_ROOT/etc/init.d/my_maintenance.d/start.d
mkdir -p $RPM_BUILD_ROOT/etc/init.d/my_maintenance.d/stop.d

%post
# the post section is where you can run commands after the rpm is installed.
insserv /etc/init.d/my_maintenance

%clean
rm -rf $RPM_BUILD_ROOT
rm -rf %{_tmppath}/%{name}
rm -rf %{_topdir}/BUILD/%{name}

# list files owned by the package here
%files

%changelog
* Sat Jun 18 2016  Pete Tanton
- 0.1 r1 First release
