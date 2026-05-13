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
        stage('Build') {
            steps {
                checkout scmGit(
                    branches: [[name: '*/b2']], 
                    extensions: [], 
                    // ⚠️ 请确认是否为你实际的 GitHub 仓库地址
                    userRemoteConfigs: [[url: 'https://github.com/luckyx7/Teedy.git']]
                )
                sh 'mvn -B -DskipTests clean package'
            }
        }

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
    }
}
