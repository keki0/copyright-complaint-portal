
pipeline {

    agent any

    parameters {
        string(
            name: 'APP_PORT',
            defaultValue: '8081',
            description: 'Port for the final Copyright Complaint Portal deployment'
        )
    }

    environment {
        APP_NAME = 'copyright-complaint-portal'
        DEPLOY_DIR = 'C:\\JenkinsDeploy\\copyright-complaint-portal'
        TEST_PORT = '8082'
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

                        echo Checking whether test port is already in use...

                        echo Checking whether test port is already in use...

                        powershell -NoProfile -Command "$pids = Get-NetTCPConnection -LocalPort %TEST_PORT% -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique; foreach ($processId in $pids) { if ($processId -ne $PID) { Write-Host ('Stopping process ' + $processId); Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue } }"

                        echo Waiting for port to become available...

                        powershell -NoProfile -Command "Start-Sleep -Seconds 3"

                        echo Starting test application...

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
                    timeout /t 5 /nobreak >nul
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
                    for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%TEST_PORT% ^| findstr LISTENING') do (
                        echo Stopping test process %%a
                        taskkill /F /PID %%a
                    )

                    echo Test instance cleanup completed.
                '''
            }
        }

        stage('Deploy') {
            steps {
                echo 'DEPLOYING TESTED APPLICATION'

                withCredentials([
                    usernamePassword(
                        credentialsId: 'mysql-credentials',
                        usernameVariable: 'DB_USERNAME',
                        passwordVariable: 'DB_PASSWORD'
                    )
                ]) {

                    bat '''
                        if not exist "%DEPLOY_DIR%" mkdir "%DEPLOY_DIR%"

                        echo COPYING APPLICATION JAR

                        copy /Y "target\\copyright-complaint-portal-0.0.1-SNAPSHOT.jar" "%DEPLOY_DIR%\\copyright-complaint-portal.jar"

                        echo STOPPING APPLICATION ON PORT %APP_PORT%...

                        powershell -NoProfile -Command "$pids = Get-NetTCPConnection -LocalPort %APP_PORT% -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique; foreach ($processId in $pids) { if ($processId -ne $PID) { Write-Host ('Stopping process ' + $processId); Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue } }"

                        powershell -NoProfile -Command "Start-Sleep -Seconds 3"

                        echo STARTING FINAL APPLICATION

                        set "JENKINS_NODE_COOKIE=dontKillMe"

                        start "CopyrightComplaintPortal" /MIN cmd /c "set DB_USERNAME=%DB_USERNAME%&& set DB_PASSWORD=%DB_PASSWORD%&& java -jar %DEPLOY_DIR%\\copyright-complaint-portal.jar --server.port=%APP_PORT% > %DEPLOY_DIR%\\application.log 2>&1"

                        echo Deployment command completed.
                    '''
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                echo 'VERIFYING FINAL DEPLOYMENT'

                bat '''
                    set ATTEMPT=1

                    :CHECK

                    echo Health check attempt %ATTEMPT% of 12...

                    curl --fail --silent http://localhost:%APP_PORT%/actuator/health

                    if not errorlevel 1 (
                        echo Final application health check passed.
                        exit /b 0
                    )

                    if %ATTEMPT% GEQ 12 (
                        echo Final application health check failed.
                        type "%DEPLOY_DIR%\\application.log"
                        exit /b 1
                    )

                    set /a ATTEMPT+=1
                    timeout /t 5 /nobreak >nul
                    goto CHECK
                '''
            }
        }
    }

    post {
        success {
            echo 'PIPELINE SUCCESSFUL'
            echo 'Selenium tests passed and application deployed.'
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
