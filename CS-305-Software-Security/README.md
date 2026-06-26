# CS 305 – Software Security Portfolio

**Author:** Matthew Randall  
**Course:** CS 305 – Software Security  
**Institution:** Southern New Hampshire University  

---

## Portfolio Artifact

This repository includes the completed **Artemis Financial Practices for Secure Software Report** (Project Two), which documents the secure coding implementations completed for the fictional client Artemis Financial. The report covers HTTPS/TLS configuration, SHA-256 checksum verification, and a full OWASP dependency-check vulnerability scan with findings and mitigation decisions.

---

## Module Eight Journal Reflection

**Client Summary**

Artemis Financial is a fictional consulting firm that manages sensitive client financial data and needed help identifying and addressing security vulnerabilities in their web-based software application. The company wanted me to assess their existing codebase for weaknesses, implement secure communication protocols, and ensure their software was handling data with the level of protection that financial clients expect and regulators require.

**Identifying Vulnerabilities**

When identifying vulnerabilities, I think I did well at being methodical — working through both a manual code review and a static analysis scan using the OWASP Dependency-Check tool to catch issues that aren't always visible to the naked eye. Coding securely matters because a single vulnerability can expose sensitive data, damage client trust, and create legal and financial liability that a company may not recover from. Security isn't just a technical checkbox; it directly supports the long-term health and credibility of the business.

**Challenges and Helpful Experiences**

The most challenging part of the vulnerability assessment was working through the OWASP dependency-check process, particularly dealing with NVD API key configuration and rate-limiting delays that caused the scan to run slowly or stall. Once I worked through those issues, the tool became genuinely useful for surfacing CVEs tied to third-party dependencies — which was one of the more eye-opening parts of the project because it showed how much risk can come from libraries you didn't write yourself.

**Increasing Layers of Security**

I increased layers of security by implementing HTTPS using a self-signed certificate generated with Java Keytool, adding SHA-256 checksum verification to validate data integrity, and suppressing false positives in the dependency-check scan with a suppression.xml file to keep the output actionable. In the future I would continue using OWASP Dependency-Check alongside tools like NIST's NVD database to assess vulnerabilities, and I'd use the CVSS scoring system to prioritize which findings require immediate mitigation versus monitoring.

**Ensuring Functionality and Security**

To make sure the application remained functional and secure after refactoring, I ran the application and verified the HTTPS endpoint was reachable, confirmed the checksum output matched expected values, and re-ran the dependency-check scan after changes to make sure I hadn't introduced new vulnerabilities. Comparing the pre- and post-refactor scan reports side by side gave me a clear picture of whether my changes had added any new risk.

**Helpful Resources and Tools**

The tools and practices I used that I expect to carry forward include Java Keytool for certificate generation, Maven with the OWASP dependency-check plugin for automated vulnerability scanning, SHA-256 hashing for data integrity verification, and the general practice of running a fresh dependency scan after any significant code change. These are directly applicable to real-world secure software development and penetration testing workflows.

**Value for Future Employers**

For future employers, I would show the Artemis Financial Practices for Secure Software Report because it demonstrates that I can do more than identify problems — I can implement solutions. It shows hands-on work with HTTPS/TLS configuration, cryptographic hashing, static analysis tooling, and professional documentation of security findings and decisions. For anyone hiring into a security-focused or compliance-sensitive role, that combination of technical execution and written communication is exactly what they'd want to see evidence of.

---

*Southern New Hampshire University – CS 305 Software Security*
