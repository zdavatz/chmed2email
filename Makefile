identifier="com.ywesee.chmed2email"

jar:
	./gradlew shadowJar
.PHONEY: jar

clean:
	./gradlew clean
.PHONEY: clean
