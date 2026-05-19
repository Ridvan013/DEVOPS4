#!groovy
// Bu dosyayi Jenkins baslamadan once suraya kopyala:
//   %JENKINS_HOME%\init.groovy.d\basic-security.groovy
// (orn. C:\Users\RIDVAN\.jenkins\init.groovy.d\basic-security.groovy)
// Setup sihirbazini atlar ve 'ridvan'/'ridvan' admin hesabini olusturur.
import jenkins.model.*
import hudson.security.*
import jenkins.install.InstallState

def instance = Jenkins.get()

def realm = new HudsonPrivateSecurityRealm(false)
instance.setSecurityRealm(realm)

if (instance.getSecurityRealm().getAllUsers().find { it.id == 'ridvan' } == null) {
    instance.getSecurityRealm().createAccount('ridvan', 'ridvan')
}

def strategy = new FullControlOnceLoggedInAuthorizationStrategy()
strategy.setAllowAnonymousRead(false)
instance.setAuthorizationStrategy(strategy)

instance.save()

if (!instance.installState.isSetupComplete()) {
    InstallState.INITIAL_SETUP_COMPLETED.initializeState()
}
