# Установка plantuml.jar
```bash
curl -L https://sourceforge.net/projects/plantuml/files/plantuml.jar/download -o plantuml.jar
````

# Генерация .png
```bash
java -Dfile.encoding=UTF-8 -jar plantuml.jar diagrams/context/context-diagram.puml
````
