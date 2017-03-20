Feature: Publisher Authentication

  Scenario Outline: Verifies a valid publisher
    Given A publish request is made for app "<app>" stream "<stream>" user "<user>" password "<password>"
    Then A "<code>" response code is returned
    Then The response has a body of "<msg>"
    Examples:
      | app     | stream     | user     | password     | code | msg                   |
      | testApp | testStream | testUser | testPassword | 403  | stream not authorised |
      |         | testStream | testUser | testPassword | 400  | Missing param: app=   |