def call(applicationName, commitHash, version) {
    def helmKubectlPodYaml = libraryResource 'podtemplates/helm-kubectl-pod.yaml'
    podTemplate(name: 'helm-kubectl', label: 'helmKubectl', yaml: helmKubectlPodYaml) {
        node(label) {
            container('helm-kubectl') {
                checkout scm
		        sh "helm lint chart/${applicationName}"
                sh "helm package --app-version ${version} --debug --save=false chart/${applicationName}"
		            script{
		              chart_tgz = sh(returnStdout: true, script: "find . -name *.tgz | tr -d '\n'")
 		            }		            
                sh """curl -L --data-binary "@${chart_tgz}" http://34.67.152.26:8080/api/charts"""
		        sh "helm init --client-only"
		        sh "helm repo add chartmuseum http://34.67.152.26:8080"
                sh "helm upgrade ${applicationName} chartmuseum/${applicationName} \
                -i --namespace cje \
                --set image.tag=${version}-${commitHash}"
                publishEvent simpleEvent("${applicationName}:${version}")

                   }

        }

    }
}
