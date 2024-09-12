## Solucion parcial Arep 1
### Instalación y despliegue
Primero debemos instalar el proyecto
```bash
mvn clean install
```
Ahora compilaremos el proyecto
```bash
mvn compile
```
Y por último ejecutaremos la calculadora en una instancia y la fachada en terminales separadas con los siguientes comandos .
```bash
java -cp target/classes org.example.Service.CalculatorService
```
```bash
java -cp target/classes org.example.Service.FachadaService
```

