pipeline {

    agent any

    parameters {
        string(
            name: 'APP_PORT',
            defaultValue: '8081',
            description: 'Port for the final Docker deployment'
        )
    }

    environment {
        APP_NAME = 'copyright-complaint-portal'
        DEPLOY_DIR = 'C:\\JenkinsDeploy\\copyright-complaint-portal'
        TEST_PORT = '8082'
        DOCKERHUB_USER = 'ketakipatil'
        DOCKER_IMAGE = 'ketakipatil/copyright-complaint-portal'
        CONTAINER_NAME = 'ccp-cd-container'
    }

    stages {

        stage('Checkout') {
            steps {
                echo 'CHECKOUT STAGE'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'BUILD STAGE'
                bat 'java -version'
                bat 'mvn -version'
                bat 'call mvn clean compile -DskipTests'
            }
        }

        stage('Package') {
            steps {
                echo 'PACKAGE STAGE'
                bat 'call mvn package -DskipTests'
                bat 'dir target'
            }
        }

        stage('Start Test Instance') {
            steps {
                echo 'STARTING TEST INSTANCE ON PORT 8082'

                withCredentials([
                    usernamePassword(
                        credentialsId: 'mysql-credentials',
                        usernameVariable: 'DB_USERNAME',
                        passwordVariable: 'DB_PASSWORD'
                    )
                ]) {
                    bat '''
                        if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"

                        copy /Y "target\\copyright-complaint-portal-0.0.1-SNAPSHOT.jar" "%DEPLOY_DIR%\\copyright-complaint-portal.jar"

                        powershell -NoProfile -Command "$pids = Get-NetTCPConnection -LocalPort %TEST_PORT% -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique; foreach ($processId in $pids) { if ($processId -ne $PID) { Write-Host ('Stopping process ' + $processId); Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue } }"

                        powershell -NoProfile -Command "Start-Sleep -Seconds 3"

                        set "JENKINS_NODE_COOKIE=dontKillMe"

                        start "CCP-Test" /MIN cmd /c "set DB_USERNAME=%DB_USERNAME%&& set DB_PASSWORD=%DB_PASSWORD%&& java -jar %DEPLOY_DIR%\\copyright-complaint-portal.jar --server.port=%TEST_PORT% > %DEPLOY_DIR%\\test-application.log 2>&1"

                        echo Test application startup command issued.
                    '''
                }
            }
        }

        stage('Verify Test Instance') {
            steps {
                echo 'VERIFYING TEST INSTANCE HEALTH'

                bat '''
                    set ATTEMPT=1

                    :CHECK

                    echo Health check attempt %ATTEMPT% of 12...

                    curl --fail --silent http://localhost:%TEST_PORT%/actuator/health

                    if not errorlevel 1 (
                        echo Test application is healthy.
                        exit /b 0
                    )

                    if %ATTEMPT% GEQ 12 (
                        echo Test application health check failed.
                        type "%DEPLOY_DIR%\\test-application.log"
                        exit /b 1
                    )

                    set /a ATTEMPT+=1
                    powershell -NoProfile -Command "Start-Sleep -Seconds 5"
                    goto CHECK
                '''
            }
        }

        stage('Selenium Continuous Testing') {
            steps {
                echo 'RUNNING SELENIUM UI TESTS'

                bat '''
                    call mvn test -Dselenium.headless=true -Dselenium.baseUrl=http://localhost:%TEST_PORT%
                '''
            }

            post {
                always {
                    echo 'PUBLISHING SELENIUM TEST RESULTS'

                    junit(
                        testResults: 'target/surefire-reports/TEST-*.xml',
                        allowEmptyResults: true
                    )

                    archiveArtifacts(
                        artifacts: 'test-screenshots/**/*.png',
                        allowEmptyArchive: true
                    )
                }
            }
        }

        stage('Stop Test Instance') {
            steps {
                echo 'STOPPING TEMPORARY TEST INSTANCE'

                bat '''
                    powershell -NoProfile -Command "$pids = Get-NetTCPConnection -LocalPort %TEST_PORT% -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique; foreach ($processId in $pids) { Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue }"

                    echo Test instance cleanup completed.
                '''
            }
        }

        stage('Build Docker Image') {
            steps {
                echo 'BUILDING VERSIONED DOCKER IMAGE'

                bat '''
                    echo Creating Docker image tags...

                    for /f %%i in ('git rev-parse --short HEAD') do set GIT_SHA=%%i

                    echo Git commit: %GIT_SHA%
                    echo Jenkins build: %BUILD_NUMBER%

                    docker build ^
                        -t %DOCKER_IMAGE%:build-%BUILD_NUMBER% ^
                        -t %DOCKER_IMAGE%:sha-%GIT_SHA% ^
                        -t %DOCKER_IMAGE%:latest ^
                        .

                    if errorlevel 1 exit /b 1

                    echo Docker image build completed.
                    docker images %DOCKER_IMAGE%
                '''
            }
        }

        stage('Push Docker Image') {
            steps {
                echo 'PUBLISHING IMAGE TO DOCKER HUB'

                withCredentials([
                    usernamePassword(
                        credentialsId: 'dockerhub-credentials',
                        usernameVariable: 'DOCKERHUB_USERNAME',
                        passwordVariable: 'DOCKERHUB_PASSWORD'
                    )
                ]) {
                    bat '''
                        echo Logging in to Docker Hub...

                        powershell -NoProfile -Command "$env:DOCKERHUB_PASSWORD | docker login --username $env:DOCKERHUB_USERNAME --password-stdin"

                        if errorlevel 1 exit /b 1

                        for /f %%i in ('git rev-parse --short HEAD') do set GIT_SHA=%%i

                        echo Pushing versioned build image...
                        docker push %DOCKER_IMAGE%:build-%BUILD_NUMBER%

                        if errorlevel 1 exit /b 1

                        echo Pushing Git SHA image...
                        docker push %DOCKER_IMAGE%:sha-%GIT_SHA%

                        if errorlevel 1 exit /b 1

                        echo Pushing latest image...
                        docker push %DOCKER_IMAGE%:latest

                        if errorlevel 1 exit /b 1

                        echo Docker Hub publishing completed.
                    '''
                }
            }
        }

        stage('Deploy Docker Container') {
            steps {
                echo 'DEPLOYING DOCKER CONTAINER'

                withCredentials([
                    usernamePassword(
                        credentialsId: 'mysql-credentials',
                        usernameVariable: 'DB_USERNAME',
                        passwordVariable: 'DB_PASSWORD'
                    )
                ]) {
                    bat '''
                        if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"

                        for /f %%i in ('git rev-parse --short HEAD') do set GIT_SHA=%%i

                        set "ENV_FILE=%DEPLOY_DIR%\\docker-deploy.env"

                        echo Checking Docker image from Docker Hub...

                        docker pull %DOCKER_IMAGE%:build-%BUILD_NUMBER%

                        if errorlevel 1 exit /b 1

                        echo Stopping previous Task 12 container, if present...

                        docker stop %CONTAINER_NAME% 2>nul

                        docker rm %CONTAINER_NAME% 2>nul

                        echo Checking whether the host port is in use...

                        powershell -NoProfile -Command "$pids = Get-NetTCPConnection -LocalPort %APP_PORT% -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique; foreach ($processId in $pids) { if ($processId -ne $PID) { Write-Host ('Stopping previous host application process ' + $processId); Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue } }"

                        powershell -NoProfile -Command "Start-Sleep -Seconds 3"

                        echo Creating temporary Docker environment file...

                        powershell -NoProfile -Command "$content = 'DB_USERNAME=' + $env:DB_USERNAME + [Environment]::NewLine + 'DB_PASSWORD=' + $env:DB_PASSWORD + [Environment]::NewLine + 'DB_URL=jdbc:mysql://host.docker.internal:3306/copyright_portal' + [Environment]::NewLine + 'SERVER_PORT=8081'; [System.IO.File]::WriteAllText($env:ENV_FILE, $content, [System.Text.Encoding]::ASCII)"

                        if errorlevel 1 exit /b 1

                        echo Starting the new Docker container...

                        docker run -d ^
                            --name %CONTAINER_NAME% ^
                            --restart unless-stopped ^
                            -p %APP_PORT%:8081 ^
                            --env-file "%ENV_FILE%" ^
                            %DOCKER_IMAGE%:build-%BUILD_NUMBER%

                        if errorlevel 1 (
                            echo Docker container startup failed.
                            docker logs %CONTAINER_NAME%
                            del /Q "%ENV_FILE%" 2>nul
                            exit /b 1
                        )

                        echo Removing temporary environment file...

                        del /Q "%ENV_FILE%"

                        echo Docker deployment command completed.
                    '''
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                echo 'VERIFYING DOCKER DEPLOYMENT HEALTH'

                bat '''
                    set ATTEMPT=1

                    :CHECK

                    echo Health check attempt %ATTEMPT% of 12...

                    curl --fail --silent http://localhost:%APP_PORT%/actuator/health

                    if not errorlevel 1 (
                        echo Docker application health check passed.
                        docker ps --filter "name=%CONTAINER_NAME%"
                        exit /b 0
                    )

                    if %ATTEMPT% GEQ 12 (
                        echo Docker application health check failed.
                        echo Container status:
                        docker ps -a --filter "name=%CONTAINER_NAME%"
                        echo Container logs:
                        docker logs %CONTAINER_NAME%
                        exit /b 1
                    )

                    set /a ATTEMPT+=1
                    powershell -NoProfile -Command "Start-Sleep -Seconds 5"
                    goto CHECK
                '''
            }
        }
    }

    post {
        success {
            echo 'PIPELINE SUCCESSFUL'
            echo 'Selenium tests passed, Docker image published, and Docker container deployed.'
        }

        failure {
            echo 'PIPELINE FAILED'
            echo 'Check Jenkins Console Output and published test reports.'
        }

        always {
            echo 'PIPELINE EXECUTION COMPLETED'
        }
    }
}
