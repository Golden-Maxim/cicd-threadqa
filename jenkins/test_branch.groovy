task_branch = "${TEST_BRANCH_NAME}".toString()
def branch_cutted = task_branch.contains("origin") ? task_branch.split('/')[1] : task_branch.trim()
currentBuild.displayName = "$branch_cutted"

//withEnv([ "branch=${branch_cutted}"]) {
    stage("Checkout Branch") {
        if (!"$branch_cutted".contains("master")) {
            try {
                echo "Working with $branch_cutted"
                sh "git clone git@gitlab.com:epickonfetka/cicd-threadqa.git"
                sh "git checkout $branch_cutted"
                sh "git merge master"
            } catch (err) {
                echo "Failed to merge master to branch $branch_cutted"
                throw("${err}")
            }
        } else {
            echo "Current branch is master"
        }
    }

    stage("Run tests") {
        testPart()
    }
//}

def testPart(){
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


