We made a few changes to the source.  Those changes (and only those changes) are in the "mgwt-2.0.0-modified"
directory here.  The original, official MGWT jar is mgwt-2.0.0.jar.  During a build, we unjar everything in
mgwt-2.0.0.jar, copy our modified files in, rebuild, create a new jar named "mgwt-2.0.0-modified.jar", and
then cleanup.