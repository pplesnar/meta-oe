DESCRIPTION = "Graphical login manager"
LICENSE = "GPLv2"
LIC_FILES_CHKSUM = "file://COPYING;md5=94d55d512a9ba36caa9b7df079bae19f"

DEPENDS = "xinput gnome-panel tcp-wrappers libcanberra libxklavier grep consolekit libpam gnome-doc-utils gtk+ xrdb"

PR = "r10"

inherit gnome update-rc.d

SRC_URI += " \
            file://cross-xdetection.diff \
            file://0001-Remove-user-switch-applet.patch \
            file://sysrooted-pkg-config.patch \
            file://0001-Don-t-use-a-null-icon.patch \
            file://0002-user-chooser-widget-fix-ref-counting-issue-on-user-p.patch \
            file://0003-user-chooser-try-to-find-fallback-icon-more-aggressi.patch \
            file://0004-greeter-fix-swapped-LHS-RHS-in-icon-loading-error-pa.patch \
            file://%gconf-tree.xml \
            file://gdm \
            file://gdm.conf \
            file://gdm-pam \
            file://Default \
           "

SRC_URI[archive.md5sum] = "dbe5187a2e17881cc454e313e0ae8d1e"
SRC_URI[archive.sha256sum] = "034d23af0ea18d86e5543e707212d9297ec7d83f221808968af266dbebc0e703"

EXTRA_OECONF = " --enable-authentication-scheme=shadow \
                 --enable-debug=yes \
                 --with-console-kit \
                 --disable-scrollkeeper "

do_install_prepend() {
	mkdir -p ${D}/var/lib/gdm/.gconf.mandatory
	cp ${WORKDIR}/%gconf-tree.xml ${D}/var/lib/gdm/.gconf.mandatory/
}

do_install_append() {
	install -d ${D}/${sysconfdir}/init.d
	install -m 0755 ${WORKDIR}/gdm ${D}/${sysconfdir}/init.d/

	install -d ${D}/${sysconfdir}/gdm
	install -m 0644 ${WORKDIR}/gdm.conf ${D}/${sysconfdir}/gdm/

	install -d ${D}/${sysconfdir}/pam.d
	install -m 0755 ${WORKDIR}/gdm-pam       ${D}/${sysconfdir}/pam.d/gdm
	rm -f ${D}/${sysconfdir}/pam.d/gdm-autologin

	install -d ${D}/${sysconfdir}/gdm/Init
	install -m 0755 ${WORKDIR}/Default ${D}/${sysconfdir}/gdm/Init

	rm -f ${D}${datadir}/gdm/autostart/LoginWindow/at-spi-registryd-wrapper.desktop
	rm -f ${D}${datadir}/gdm/autostart/LoginWindow/orca-screen-reader.desktop
	rm -f ${D}${datadir}/gdm/autostart/LoginWindow/gnome-mag.desktop
	rm -f ${D}${datadir}/gdm/autostart/LoginWindow/gok.desktop
	rm -f ${D}${datadir}/gdm/autostart/LoginWindow/metacity.desktop
}

FILES_${PN} += "${datadir}/icon* \
		${datadir}/xsession* \
               "

RDEPENDS_${PN} += "grep dbus-x11 shadow"
# "libpam-base-files"
CONFFILES_${PN} += "${sysconfdir}/gdm/gdm.conf ${sysconfdir}/init.d/gdm"
RRECOMMENDS_${PN} += "openssh-misc desktop-file-utils glib-2.0-utils metacity gnome-session polkit-gnome consolekit"

INITSCRIPT_NAME = "gdm"
INITSCRIPT_PARAMS = "start 99 5 2 . stop 20 0 1 6 ."

pkg_postinst_${PN} () {
    # can't do this offline
    if [ "x$D" != "x" ]; then
        exit 1
    fi
    grep "^gdm:" /etc/group > /dev/null || addgroup gdm
    grep "^gdm:" /etc/passwd > /dev/null || adduser --disabled-password --system --home /var/lib/gdm gdm --ingroup gdm -g gdm

if [ -d /var/lib/gdm ]; then
  chown -R gdm:gdm /var/lib/gdm
  chmod 0750 /var/lib/gdm
fi

# Register up as default dm
mkdir -p ${sysconfdir}/X11/
echo "${bindir}/gdm" > ${sysconfdir}/X11/default-display-manager

}

pkg_postrm_${PN} () {
    deluser gdm || true
    delgroup gdm || true
	sed -i /gdm/d ${sysconfdir}/X11/default-display-manager || true
}




