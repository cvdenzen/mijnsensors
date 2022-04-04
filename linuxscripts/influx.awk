
#
# Set onderwerp op command line: gawk -v onderwerp=Hester
#
# Doe het per onderwerp ivm performance. Dus viermaal opstarten.
#
BEGIN {
 print "# DDL\n\n# DML\n# CONTEXT-DATABASE: vitals\n\n"
}
#
# 7 uur in de ochtend in gmt is 9 uur lokale tijd.
/^[0-9]/ {
  clean=gensub(/\"([0-9]*),([0-9]*)\"/,"\\1.\\2","g");
  # Carl=6 Hester=7 Arjan=8 Martin=9
  b["Carl"]=6
  b["Hester"]=7
  b["Arjan"]=8
  b["Martin"]=9
  onderwerpnr=b[onderwerp]
  split(clean,a,/"?,"?/);
  a[onderwerpnr]=gensub("\"","","g",a[onderwerpnr]);
  a[onderwerpnr]=gensub(",",".","g",a[onderwerpnr]);
  if (a[onderwerpnr]!="") {
    print "lichaamsgewicht,onderwerp="onderwerp" gewicht="a[onderwerpnr]" "mktime(a[2]" "a[3]" "a[4]" 7 0 0");
  }
}
#
#
