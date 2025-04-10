# chmed2email
chmed2email

## Build

```
make jar
```

## Run

Convert from a CHMED16A string:

```
$ java -jar ./build/libs/chmed2email-1.0-SNAPSHOT-all.jar -c CHMED16A....

<?xml version="1.0" ?><prescription .....</prescription>
```

Convert by scanning QRCode image from a PDF:

```
$ java -jar ./build/libs/chmed2email-1.0-SNAPSHOT-all.jar -p xxxxx.PDF

Found QRCode in PDF CHMED16A1....
<?xml version="1.0" ?><prescription ....</prescription>
```
