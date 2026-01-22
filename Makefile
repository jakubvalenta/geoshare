variant := free

.PHONY: build
build: | check-env
	rm -r "app/build/outputs/apk/$(variant)/release" || true
	./gradlew "assemble$(variant)Release"
	zipalign -v -p 4 \
		"app/build/outputs/apk/$(variant)/release/app-$(variant)-release-unsigned.apk" \
		"app/build/outputs/apk/$(variant)/release/app-$(variant)-release-unsigned-aligned.apk"
	apksigner sign \
		--ks "$(STORE_FILE)" \
		--ks-pass env:STORE_PASSWORD \
		--ks-key-alias "$(KEY_ALIAS)" \
		--key-pass env:KEY_PASSWORD \
		--out \
		"app/build/outputs/apk/$(variant)/release/app-$(variant)-release.apk" \
		"app/build/outputs/apk/$(variant)/release/app-$(variant)-release-unsigned-aligned.apk"

.PHONY: bundle
bundle: | check-env
	./gradlew "bundle$(variant)Release" \
		-Pandroid.injected.signing.store.file="$(STORE_FILE)" \
		-Pandroid.injected.signing.store.password="$(STORE_PASSWORD)" \
		-Pandroid.injected.signing.key.alias="$(KEY_ALIAS)" \
		-Pandroid.injected.signing.key.password="$(KEY_PASSWORD)"

.PHONY: check-env
check-env:
ifeq ($(STORE_FILE),)
	@echo "Variable STORE_FILE is not set."
	@echo "Example: STORE_FILE=path/to/keystore.js"
	exit 1
endif
ifeq ($(STORE_PASSWORD),)
	@echo "Variable STORE_PASSWORD is not set."
	@echo "Example: STORE_PASSWORD=mypassword"
	exit 1
endif
ifeq ($(KEY_ALIAS),)
	@echo "Variable KEY_ALIAS is not set."
	@echo "Example: KEY_ALIAS=com.example.android"
	exit 1
endif
ifeq ($(KEY_PASSWORD),)
	@echo "Variable KEY_PASSWORD is not set."
	@echo "Example: KEY_PASSWORD=mypassword"
	exit 1
endif
