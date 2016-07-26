# A very basic regression-check suite for WadC. Drop test .wl files in here
# and you can see if the output WAD changes. Managing whether it *should*
# have changed is left up to you.

# hint: md5sha1sum via brew for OS X

JAR  := target/wadc-2.0-SNAPSHOT.jar
WADS := $(patsubst %.wl,%.wad, $(wildcard examples/*.wl) $(wildcard tests/*.wl))

default: check

check:
	sha1sum -b -c sha1sums

wads: $(WADS)

%.wad : %.wl
	java -cp $(JAR) org.redmars.wadc.WadCCLI -nosrc "$<"

# this should not be automatically re-generated so it should not appear as
# a dependency in any other rules. To be run by hand by someone who is very
# confident they haven't broken WadC at the time they run it :)
sha1sums: $(WADS)
	sha1sum -b $(WADS) > "$@"

clean:
	rm -f $(WADS)

.PHONY: default clean check wads