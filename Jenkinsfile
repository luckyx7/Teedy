pipeline {
    agent any

    environment {
        // 🐳 Docker Hub 镜像地址（已替换为你的用户名）
        DOCKER_IMAGE = 'lucky24x/teedy-app'
        // 🏷️ 使用 Jenkins 构建号作为 Tag
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        // 🔑 你的 Jenkins 凭证 ID
        DOCKER_CRED_ID = 'c64180da-1df5-490d-85ef-9337f7964c31'
    }

    stages {
        // 📥 拉取代码（必须放在最前面）
        stage('Checkout') {
            steps {
                checkout scmGit(
                    branches: [[name: '*/master']], 
                    extensions: [], 
                    // ⚠️ 请确认是否为你实际的 GitHub 仓库地址
                    userRemoteConfigs: [[url: 'https://github.com/luckyx7/Teedy.git']]
                )
            }
        }

        // 🛠️ 保留你原有的 Maven 流程（适合生成 PMD/JaCoCo/Site 报告）
        stage('Clean') { steps { sh 'mvn clean' } }
        stage('Compile') { steps { sh 'mvn compile' } }
        stage('Test') { steps { sh 'mvn test -Dmaven.test.failure.ignore=true' } }
        stage('PMD') { steps { sh 'mvn pmd:pmd' } }
        stage('JaCoCo') { steps { sh 'mvn jacoco:report' } }
        stage('Site') { steps { sh 'mvn site' } }
        stage('Package') { steps { sh 'mvn package -DskipTests' } }

        // 🐳 构建 Docker 镜像
        stage('Building Docker Image') {
            steps {
                script {
                    docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}")
                }
            }
        }

        // 📤 推送镜像到 Docker Hub
        stage('Upload Image') {
            steps {
                script {
                    docker.withRegistry('https://registry.hub.docker.com', env.DOCKER_CRED_ID) {
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push()
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push('latest')
                    }
                }
            }
        }

        // 🚀 启动 3 个容器（端口 8082 / 8083 / 8084）
        stage('Run 3 Containers') {
            steps {
                script {
                    def ports = [8082, 8083, 8084]
                    ports.each { port ->
                        def containerName = "teedy-container-${port}"
                        sh "docker stop ${containerName} || true"
                        sh "docker rm ${containerName} || true"
                        docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").run(
                            "--name ${containerName} -d -p ${port}:8080"
                        )
                        echo "✅ Started ${containerName} on port ${port}"
                    }
                }
            }
        }

        // 🔍 验证容器状态（方便截图交作业）
        stage('Verify') {
            steps {
                sh 'docker ps --filter "name=teedy-container" --format "table {{.Names}}\t{{.Ports}}\t{{.Status}}"'
            }
        }
    }

    // 📦 保留你原有的构建后处理逻辑
    post {
        always {
            archiveArtifacts artifacts: '**/target/site/**/*.*', fingerprint: true
            archiveArtifacts artifacts: '**/target/**/*.jar', fingerprint: true
            archiveArtifacts artifacts: '**/target/**/*.war', fingerprint: true
            junit '**/target/surefire-reports/*.xml'
            // 可选：清理本地旧镜像防磁盘打满
            sh 'docker images lucky24x/teedy-app --format "{{.Tag}}\t{{.ID}}" | tail -n +6 | cut -f2 | xargs -r docker rmi 2>/dev/null || true'
        }
        failure {
            echo '❌ Pipeline failed! 请检查 Console Output 定位错误。'
        }
    }
}
