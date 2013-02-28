#!/bin/sh -x

# convert a single svg file into a png in the according drawable dir
svg2png() {
	basename=$1
	width=$2
	height=$3
	dest=$4
	inkscape --export-png=res/$dest/$basename.png --export-width=$width --export-height=$height --export-background-opacity=0,0 -C -z asset-graphics/$basename.svg
}

svg2png_rel() {
	basename=$1
	dpi=$2
	dest=$3
	inkscape --export-png=res/$dest/$basename.png --export-dpi=$dpi --export-background-opacity=0,0 -C -z asset-graphics/$basename.svg
}

svg2icon() {
	basename=$1

	svg2png $basename 36 36 drawable-ldpi
	svg2png $basename 48 48 drawable
	svg2png $basename 72 72 drawable-hdpi
	svg2png $basename 96 96 drawable-xhdpi
}

# a roster status icon is 66% of a regular icon
svg2status() {
	basename=$1

	svg2png $basename 24 24 drawable-ldpi
	svg2png $basename 32 32 drawable
	svg2png $basename 48 48 drawable-hdpi
	svg2png $basename 64 64 drawable-xhdpi
}

# a status bar icon is 24x38 on hdpi, and has according downscaled sizes
svg2sbar() {
	basename=$1

	svg2png $basename 12 19 drawable-ldpi
	svg2png $basename 16 25 drawable
	svg2png $basename 24 38 drawable-hdpi
}

svg2rel() {
	basename=$1

	svg2png_rel $basename 100 drawable-ldpi
	svg2png_rel $basename 200 drawable
	svg2png_rel $basename 300 drawable-hdpi
	svg2png_rel $basename 400 drawable-xhdpi
}

## convert icon
#svg2icon icon
#
## convert statusbar notification icon
#svg2sbar sb_message
#
## convert status
## convert paw status
#for file in `ls asset-graphics/ic_*.svg`
#do
#	basename=`basename $file .svg`
#	svg2status $basename
#done

for file in `ls asset-graphics/rel_*.svg`
do
	basename=`basename $file .svg`
	svg2rel $basename
done
