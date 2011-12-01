#
# Spec file for the EMIRD - EMIR Client Daemon
#
Summary: EMIRD - EMIR Client Daemon
Name: emird
Version: 1.0
Release: 1
License: CC-BY-SA
Group: Infrastructure Services
URL: https://github.com/eu-emi/emiregistry
BuildArch: noarch
Packager: EMI emir@niif.hu
Requires: python >= 2.4.3, python-simplejson
BuildRoot: %{_tmppath}/%{name}-%{version}

%description
The EMIRD is a daemon like service that can be executed next to the EMI
services (preferably on the same machine) that are unable to register
themselves into to EMIR Infrastucture.
It behaves as an ordinary client ( uses exactly the same, standard
RESTful API as the other clients would do) when perform the automatical
and periodical registration and update against the configured EMI
Registry service instead of the services or other manual administration
tools.

This package contains the EMIR Client Daemon.


%changelog
* Thu Dec 1 2011 Ivan Marton <martoni@niif.hu>
- Initial RPM package


%prep
rm -rf %{buildroot}/%{name}-%{version}
git clone git://github.com/eu-emi/emiregistry.git %{buildroot}/emiregistry/
install -d %{buildroot}%{_libdir}/emi/emird/
install -d %{buildroot}%{_sysconfdir}/emi/emird/
install -d %{buildroot}%{_bindir}
install -d %{buildroot}%{_defaultdocdir}/%{name}-%{version}
install -d %{buildroot}/var/log/emi/emird
install -m 0644 %{buildroot}/emiregistry/emird/daemon.py %{buildroot}%{_libdir}/emi/emird/
install -m 0644 %{buildroot}/emiregistry/emird/EMIR.py %{buildroot}%{_libdir}/emi/emird/
install -m 0644 %{buildroot}/emiregistry/emird/emird.ini %{buildroot}%{_sysconfdir}/emi/emird/
install -m 0644 %{buildroot}/emiregistry/emird/docs/README %{buildroot}%{_defaultdocdir}/%{name}-%{version}/
install -m 0644 %{buildroot}/emiregistry/emird/docs/example.json %{buildroot}%{_defaultdocdir}/%{name}-%{version}/
install -m 0755 %{buildroot}/emiregistry/emird/emird.py %{Obuildroot}%{_bindir}/
rm -rf %{buildroot}/emiregistry


%files
%defattr(755, emi, emi, -)
#
# Config files
#
%dir %attr(755 emi emi) "%{_sysconfdir}/emi/emird"
%config(noreplace) %attr(0644 emi emi) "%{_sysconfdir}/emi/emird/emird.ini"
#
# Log files
#
%dir %attr(0700 emi emi) "/var/log/emi/emird"
#
# Lib files
#
%attr(644 root root) %dir "%{_libdir}/emi/emird"
%attr(644 root root) "%{_libdir}/emi/emird/*.py"
#
# Documentation
#
%doc %{_defaultdocdir}/%{name}-%{version}/README
%doc %{_defaultdocdir}/%{name}-%{version}/example.json
#
# Executable
#
%attr(0755 emi emi) "%{_bindir}/emird.py"
%pre
/usr/sbin/groupadd -r emi 2>/dev/null || :
/usr/sbin/useradd -c "EMI" -g emi \
    -s /sbin/nologin -r -d %{_datadir}/emi emi 2>/dev/null || :

%post
if [ -e /sbin/chkconfig ]; then
    /sbin/chkconfig --add emi-emird
elif [ -e /sbin/insserv ]; then
    /sbin/insserv emi-emird
fi

%preun
if [ -e /sbin/chkconfig ]; then
  if [ "$1" = "0" ]; then 
      /etc/init.d/emi-emird stop >/dev/null 2>&1
      /sbin/chkconfig --del emi-emird
  fi
elif [ -e /sbin/insserv ]; then
  if [ "$1" = "0" ]; then 
      /etc/init.d/emi-emird stop >/dev/null 2>&1
      /sbin/insserv -r emi-emird
  fi
fi

