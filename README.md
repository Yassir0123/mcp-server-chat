# Compte Rendu

Ce projet consiste en la création d’un système serveur–client MCP, équipé d’un modèle de langage (LLM) permettant le traitement intelligent des requêtes grâce à des outils préconfigurés. L’ensemble offre une architecture robuste pour l’analyse et l’exécution de commandes de manière automatisée et contextuelle.

# 1) Création du MCP server et Tools
- Après avoir finit la première étape qui est la création du mcp-serveur ainsi que ses tools, definit par @Tool, j'ai utilisé postman pour simuler les requêtes de clients. 
- Premièrement, voici les tools qui on été récuperer :
![firstimage](https://github.com/user-attachments/assets/5e4ae0c6-4036-4d97-8dc8-ae0ab05114f7)
![secondimage](https://github.com/user-attachments/assets/79765e98-9151-4770-8c96-0cf5fa8d95e5)
![thirdimage](https://github.com/user-attachments/assets/b299c3f4-d847-4c0d-afd9-b0c237c0dc56)

- Ensuite j'ai simuler à travers les images suivantes le processus d'initialization et d'envoie/reception des tools par le client à travers de requêtes envoyer au serveur.
-  La façon dont cela marche commence par l'initiation de la requette http://localhost:8085/sse du client vers le serveur pour ouvrir une connexion SSE (Server-Sent Events) pour recevoir les mise à jours et changements envoyé par le serveur en temps réel, en sachant que ce dernier nous fournis un token d'authentification, ainsi qu'une id de session
-  Après avoir reçu l'identifiant et le token, le client envoie ensuite une requête vers le server pour l'informer de sa présence, tout en ajoutant quelque informations sur lui même. Le server, suivra par l'envoie d'une réponse qui sera automatiquement reçu dans l'interface get du client, ce qui montre qu'ils sont maintenant relié l'un à l'autre
-  Suivant la connection au serveur, le client envoiera maintenant une demande tools/list de façon à recevoir les tools initié au niveau du mcp-server au début. Le server envoiera par la suite automatiquement les tools au client.
-  Enfin pour valider que les tools marche, on a éffectuer un test sur l'une de ceux reçu, tel que le résultat est reçu auprès du client.
-  Les images ci-dessous illustre en ordre les étapes effectuer et leur preuve de réussite.
![fourthimage](https://github.com/user-attachments/assets/e7958988-856e-47a9-b8d0-c57966ee604f)
![fifthimage](https://github.com/user-attachments/assets/686a26e2-e951-4a89-8d6d-f243b29be12a)
![sixthimage](https://github.com/user-attachments/assets/131b0ae5-6018-44e8-bdd6-ab6df3787432)
![seventhimage](https://github.com/user-attachments/assets/931986aa-b16c-459d-95b8-f9ee64893101)
![eigthimage](https://github.com/user-attachments/assets/f237377e-694a-465c-8bd6-e12650b068cb)
![ninethimage](https://github.com/user-attachments/assets/f72b4d77-d644-4916-a289-03a363161e2b)
![tenthimage](https://github.com/user-attachments/assets/e54debf1-ce1f-42cc-8005-bdbac4675d33)
![eleventhimage-toolsreceived](https://github.com/user-attachments/assets/1246588b-7482-447c-9398-c178d03e0aa4)
![twelve-toolscallsent](https://github.com/user-attachments/assets/b4440673-866b-4965-97fa-e61374ba7299)
![thirtheenimage-got the tool called for](https://github.com/user-attachments/assets/dde246f4-4040-48e4-9d68-480d8b8f2fdc)

# 2) Création du client

- Une fois le serveur MCP créé et testé via Postman, je suis passé à la mise en place d’un ensemble de clients MCP développés en Java, Node.js et Python, tous reliés à un serveur central à travers un fichier de configuration mcp-servers.json.
Pour initier l’architecture côté client, j’ai d’abord créé un contrôleur REST nommé AiAgentController, qui reçoit une requête GET contenant une question (query) de l’utilisateur. Cette question est transmise à la classe AIAgent, que j’ai également développée. Cette dernière se charge d’afficher les outils MCP disponibles (à des fins de débogage) et délègue ensuite le traitement au GroqService.
Le GroqService utilise l’API gratuite fournie par Groq, basée sur le modèle LLM LLaMA, pour interpréter la requête. Il s’appuie sur un ensemble de prétraitements configurés (system message, outils MCP, etc.) afin de guider le modèle et garantir des réponses fiables et précises, basées uniquement sur les données du projet.
En cas de question hors périmètre ou de manque de données dans le projet, des messages d’erreur clairs sont renvoyés à l’utilisateur.
- Ci-dessous l'image de la réussite d'execution du client ainsi que la réception de getcompanybyname:
![fourtheenthimage-mcpclientworks](https://github.com/user-attachments/assets/b5b4ebb8-7094-4554-997a-997170309829)
![fiftheenthimage-received content from getcompantbynametool](https://github.com/user-attachments/assets/f1edb62f-8ccb-4c5a-a62a-e398fdb9febb)
- Preuve de réussite du mcp-server json :
![sixtheenimage-mcpserverjson work](https://github.com/user-attachments/assets/8fc63b11-504d-49c7-b902-303f8411b436)
- Processus d'initialization de l'environement de python à travers les commandes, avec preuve de réussite d'execution de son server :
![seventheenimage-initializationofpythonmodule UV](https://github.com/user-attachments/assets/d53940de-4ffc-495e-b4ae-b1095435bfac)
![eighteenimage-mcpworkpython](https://github.com/user-attachments/assets/84f03f59-d7d0-47f7-b0d7-272f8229a9e7)
- Preuve de réussite suite a l'essayage de swagger ui pour envoyer une requete query :
![ninethingimage](https://github.com/user-attachments/assets/b01d1743-ee1a-4f3b-b009-38462b93fc99)

# 3) Création d'interface chat web :
![tweeny1](https://github.com/user-attachments/assets/ec8e5e7f-854f-4143-a92f-7b651e6101e2)

# 4) Explication du processus de gestion de query du client vers le server en utilisant groq

Lorsqu’un utilisateur soumet une requête via l’interface web, celle-ci est transmise au contrôleur d’agent (GroqService). Envoyer cette requête seule au LLM entraînerait des réponses génériques ou erronées. Ajouter uniquement un message système permet de restreindre le comportement du modèle, mais sans accès explicite aux outils disponibles, cela reste insuffisant.
Pour résoudre cela, le service utilise getMcpToolsForGroq() afin d’extraire, via les clients MCP, la liste des outils annotés avec @Tool. Il construit ensuite un prompt complet composé du message utilisateur, du message système (définissant les règles d’usage), et de la description des outils (nom, description, schéma).
Ce prompt est envoyé une première fois à l’API Groq. Si le modèle identifie qu’un ou plusieurs outils sont nécessaires, il renvoie une instruction de type tool_call. Ces appels sont ensuite exécutés localement via executeMcpTool, les résultats sont intégrés à l’historique de conversation.
Enfin, une seconde requête est envoyée à Groq avec le contexte enrichi (requête initiale, outils utilisés, réponses obtenues). Le modèle peut alors formuler une réponse précise, fondée uniquement sur les données projet et les outils définis, sans recours à des connaissances générales.










