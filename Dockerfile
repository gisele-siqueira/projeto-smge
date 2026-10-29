# ==============================================================================
# Estágio 1: Build da aplicação Spring Boot utilizando Maven e JDK 17
# ==============================================================================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copia apenas o pom.xml para aproveitar o cache de camadas do Docker
COPY pom.xml .

# Pré-baixa dependências do Maven (ignora falhas de plugins dinâmicos se houver)
RUN mvn dependency:go-offline -B -Dskip.npm -Dskip.installnodenpm || true

# Cria diretório frontend para compatibilidade com o frontend-maven-plugin
RUN mkdir -p frontend

# Copia o código-fonte Java e recursos do backend
COPY src ./src

# Compila o backend e empacota o JAR pulando testes e o build do front-end
RUN mvn clean package -DskipTests -Dskip.npm -Dskip.installnodenpm

# Padroniza o nome do JAR gerado para facilitar a cópia
RUN cp target/smge-*.jar /app/app.jar

# ==============================================================================
# Estágio 2: Imagem final de execução (JRE 17 enxuta e segura)
# ==============================================================================
FROM eclipse-temurin:17-jre-jammy AS runtime

WORKDIR /app

# Cria usuário e grupo não-root para execução segura da aplicação
RUN groupadd -r smge && useradd -r -g smge smge

# Copia apenas o artefato JAR compilado do estágio de build
COPY --from=builder /app/app.jar app.jar

# Ajusta permissões do arquivo
RUN chown smge:smge app.jar

# Executa com usuário não-root
USER smge

# Porta padrão utilizada pela aplicação Spring Boot
EXPOSE 8080

# Configurações de JVM otimizadas para ambientes conteinerizados
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
