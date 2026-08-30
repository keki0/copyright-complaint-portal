pipeline {

    agent any

    parameters {
        string(
            name: 'APP_PORT',
            defaultValue: '8081',
            description: 'Port on which the Copyright Complaint Portal will run'
        )
    }

    environment {
        APP_NAME = 'copyright-complaint-portal'
        DEPLOY_DIR = 'C:\\JenkinsDeploy\\copyright-complaint-portal'
    }

    stages {

        stage('Checkout') {
            steps {
                echo '========================================'
                echo 'CHECKOUT STAGE'
                echo '========================================'

                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo '========================================'
                echo 'BUILD STAGE'
                echo '========================================'

                bat 'java -version'
                bat 'mvn -version'

                bat 'call mvn clean compile -DskipTests'
            }
        }

        stage('Package') {
            steps {
                echo '========================================'
                echo 'PACKAGE STAGE'
                echo '========================================'

                bat 'call mvn package -DskipTests'

                bat 'dir target'
            }
        }

        stage('Deploy') {
            steps {
                echo '========================================'
                echo 'DEPLOY STAGE'
                echo '========================================'

                bat '''
                if not exist "C:\\JenkinsDeploy\\copyright-complaint-portal" mkdir "C:\\JenkinsDeploy\\copyright-complaint-portal"

                echo Copying application JAR...
                copy /Y "target\\copyright-complaint-portal-0.0.1-SNAPSHOT.jar" "C:\\JenkinsDeploy\\copyright-complaint-portal\\copyright-complaint-portal.jar"

                echo.
                echo Stopping previous application instance...
                taskkill /F /FI "WINDOWTITLE eq CopyrightComplaintPortal" >nul 2>&1

                echo.
                echo Starting application on port %APP_PORT%...

                start "CopyrightComplaintPortal" /MIN cmd /c "java -jar C:\\JenkinsDeploy\\copyright-complaint-portal\\copyright-complaint-portal.jar --server.port=%APP_PORT%"

                echo.
                echo Application deployment command completed.
                '''
            }
        }

        stage('Verify Deployment') {
            steps {
                echo '========================================'
                echo 'VERIFY DEPLOYMENT'
                echo '========================================'

                bat '''
                powershell -Command "Start-Sleep -Seconds 10"

                echo Checking application endpoint...

                powershell -Command "try { $r = Invoke-WebRequest -Uri 'http://localhost:%APP_PORT%/actuator/health' -UseBasicParsing; Write-Host ('HTTP Status: ' + $r.StatusCode); Write-Host $r.Content } catch { Write-Host 'Application health check failed'; exit 1 }"
                '''
            }
        }
    }

    post {
        success {
            echo '========================================'
            echo 'PIPELINE SUCCESSFUL'
            echo 'Application deployed successfully.'
            echo '========================================'
        }

        failure {
            echo '========================================'
            echo 'PIPELINE FAILED'
            echo 'Check the stage logs for details.'
            echo '========================================'
        }
    }
}