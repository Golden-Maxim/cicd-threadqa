task_branch = "${TEST_BRANCH_NAME}"
def branch = task_branch.contains("origin") ? task_branch.split('/')[1] : task_branch.trim()
currentBuild.displayName = "$branch"

def downloadProject(String repo, String branch) {
    cleanWs()
    checkout scm: [
            $class: 'GitSCM', branches: [[name: branch]],
            userRemoteConfigs: [[
                                        url: repo
                                ]]
    ]
}


withEnv([ "branch=${branch}"]) {
    stage("Merge Master") {
        if (!"$branch".contains("master")) {
            try {
                downloadProject("git@gitlab.com:epickonfetka/cicd-threadqa.git".toString(), "$branch".toString())
                sh "git checkout $branch"
                sh "git merge master"
            } catch (err) {
                echo "Failed to merge master to branch $branch"
                throw("${err}")
            }
        } else {
            echo "Current branch is master"
        }
    }

    stage("Run tests") {
        testPart()
    }
}

def testPart(){
    downloadProject("git@gitlab.com:epickonfetka/cicd-threadqa.git", "$branch")
    try {
        sh "./gradlew clean testme"
    } catch (err){
        echo "some test are failed"
        throw("${err}")
    } finally {
        sh "./gradlew allureReport"
        sh "zip -r report.zip build/reports/allure-report/allureReport/*"
        echo "Stage was finished"
    }
}

