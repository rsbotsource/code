CC=javac
CFLAGS=-g:none
SRC=src
LIB=lib
RES=resources
BINDIR=sbin
LSTF=temp.txt
IMGDIR=$(RES)/images
MANIFEST=$(RES)/Manifest.txt
NAME=RSBot
DIST=$(LIB)/$(NAME).jar

.PHONY: all Bot Bundle clean

all: Bundle

Bot:
	@if [ ! -d "$(BINDIR)" ]; then mkdir "$(BINDIR)"; fi
	$(CC) $(CFLAGS) -d "$(BINDIR)" `find "$(SRC)" -name \*.java`

Bundle: Bot
	@rm -fv "$(LSTF)"
	@cp "$(MANIFEST)" "$(LSTF)"
	@if [ -e "$(DIST)" ]; then rm -fv "$(DIST)"; fi
	jar cfm "$(DIST)" "$(LSTF)" -C "$(BINDIR)" . "license.txt" "$(IMGDIR)"/*.png
	@rm -f "$(LSTF)"

clean:
	@rm -fv "$(DIST)"
	@rm -rfv "$(BINDIR)"
