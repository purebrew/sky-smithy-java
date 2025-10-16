# sky‑smithy‑java

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 🚀 Overview
**sky‑smithy‑java** is a Java / Scala / Smithy‑tooling / microservice template / toolkit repository that collects various services, protocols, and deployment infrastructure for building microservices based on the Smithy specification.
It includes multiple sub‑projects (protocol definitions, service implementations, API gateway, swagger UI, etc.) to provide a full‑stack reference or starter project for microservice architectures.

## 📁 Repository structure
| Folder / component | Description |
|---|---|
| `protocol` | Smithy models / service / type definitions, shared protocol definitions. |
| `coffee-service` | Example microservice implementation (Java or Scala) that implements one of the protocols / models. |
| `api-registry` | Service or component acting as a registry / directory of APIs. |
| `apigw-krakend` | API gateway implementation using Krakend (or similar) to route and orchestrate microservices. |
| `swagger-ui` | API documentation UI (Swagger / OpenAPI) that surfaces the spec from the protocol / service definitions. |
| other files | Dev / build scripts: `build-all.sh`, `compose.yaml` for local / container orchestration, etc. |

## 🧰 Features
- Shared Smithy model definitions to define APIs / data types / protocols.
- Example microservice implementation (coffee service) to show how to implement an API.
- API registry to discover or aggregate microservices.
- API gateway for routing, aggregating, or transforming API calls.
- Swagger UI for documentation and interactive testing.
- Container / composition support via Docker / docker‑compose for dev or integration testing.

## 💡 Getting started

1. Clone the repository:
   ```bash
   git clone https://github.com/purebrew/sky-smithy-java.git
   cd sky-smithy-java
   ```

2. Build all components:
   ```bash
   ./build-all.sh
   ```

3. Start services locally (docker / compose):
   ```bash
   docker compose up
   ```

4. Access the API documentation:
   Open your browser at `http://localhost:7777` to use the Swagger UI and explore defined APIs.

5. Try the example coffee‑service for sample endpoints (e.g. creating coffee, querying coffee).

## 🔧 Development
- Modify / extend the Smithy models in the `protocol` folder to add new API definitions or types.
- Implement new microservices in the pattern of the `coffee-service` (or create new ones).
- Configure the API gateway (`apigw-krakend`) to route to new service endpoints.
- Keep API registry in sync so clients / gateway / docs can discover all services.

## 📦 Deployment
- The code is containerized; the `compose.yaml` shows how to spin up all services for staging / production.
- Ensure to push Docker images / update gateway routes and API registry config accordingly.

## 📄 Contribution
Contributions are welcome!
1. Fork the repo
2. Create a new branch for your feature or bug fix
3. Submit a pull request
Please follow the contribution guidelines or template (you may consider adding a `CONTRIBUTING.md`).

## 📜 License
This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

---

*Built with ❤️ by the maintainers: Schmeedy and Michal Janousek.*
