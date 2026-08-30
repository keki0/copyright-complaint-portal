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

                withCredentials([
                    usernamePassword(
                        credentialsId: 'mysql-credentials',
                        usernameVariable: 'DB_USERNAME',
                        passwordVariable: 'DB_PASSWORD'
                    )
                ]) {

                    bat '''
                        if not exist "C:\\JenkinsDeploy\\copyright-complaint-portal" mkdir "C:\\JenkinsDeploy\\copyright-complaint-portal"

                        echo Copying application JAR...

                        copy /Y "target\\copyright-complaint-portal-0.0.1-SNAPSHOT.jar" "C:\\JenkinsDeploy\\copyright-complaint-portal\\copyright-complaint-portal.jar"

                        echo.
                        echo Stopping previous application instance...

                        taskkill /F /FI "WINDOWTITLE eq CopyrightComplaintPortal" >nul 2>&1

                        echo.
                        echo Starting application on port %APP_PORT%...

                        start "CopyrightComplaintPortal" /MIN cmd /c "set DB_USERNAME=%DB_USERNAME%&& set DB_PASSWORD=%DB_PASSWORD%&& java -jar C:\\JenkinsDeploy\\copyright-complaint-portal\\copyright-complaint-portal.jar --server.port=%APP_PORT%"

                        echo.
                        echo Application deployment command completed.

                        echo.
                        echo Checking deployed JAR...

                        dir "C:\\JenkinsDeploy\\copyright-complaint-portal"

                        echo.
                        echo Checking Java process...

                        tasklist | findstr /I "java.exe"
                    '''
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                echo '========================================'
                echo 'VERIFY DEPLOYMENT'
                echo '========================================'

                bat '''
                    echo Waiting for application to start...

                    set ATTEMPT=1

                    :CHECK

                    echo Health check attempt %ATTEMPT% of 12...

                    curl --fail --silent http://localhost:%APP_PORT%/actuator/health

                    if not errorlevel 1 (
                        echo.
                        echo ========================================
                        echo APPLICATION HEALTH CHECK PASSED
                        echo ========================================
                        exit /b 0
                    )

                    if %ATTEMPT% GEQ 12 (
                        echo.
                        echo ========================================
                        echo APPLICATION HEALTH CHECK FAILED
                        echo ========================================
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